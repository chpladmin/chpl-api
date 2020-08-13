package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.util.EmailBuilder;
import net.sf.ehcache.CacheManager;

public class SplitDeveloperJob implements Job {
    public static final String JOB_NAME = "splitDeveloperJob";
    public static final String OLD_DEVELOPER_KEY = "oldDeveloper";
    public static final String NEW_DEVELOPER_KEY = "newDeveloper";
    public static final String PRODUCT_IDS_TO_MOVE_KEY = "productIdsToMove";
    public static final String USER_KEY = "user";
    private static final Logger LOGGER = LogManager.getLogger("splitDeveloperJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private SchedulerCertifiedProductSearchDetailsAsync schedulerCertifiedProductSearchDetailsAsync;

    @Autowired
    private ActivityManager activityManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Split Developer job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);

            DeveloperDTO oldDeveloper = (DeveloperDTO) jobDataMap.get(OLD_DEVELOPER_KEY);
            DeveloperDTO newDeveloper = (DeveloperDTO) jobDataMap.get(NEW_DEVELOPER_KEY);
            List<Long> productIdsToMove = (List<Long>) jobDataMap.get(PRODUCT_IDS_TO_MOVE_KEY);
            DeveloperDTO createdDeveloper = null;
            Exception splitException = null;
            try {
                createdDeveloper = splitDeveloper(oldDeveloper, newDeveloper, productIdsToMove);
            } catch (Exception e) {
                LOGGER.error("Error completing split of old developer '" + oldDeveloper.getName() + "' to new developer '"
                        + newDeveloper.getName() + "'.", e);
                splitException = e;
            }

            if (createdDeveloper != null) {
                CacheManager.getInstance().clearAll();
            }

            //send email about success/failure of job
            if (!StringUtils.isEmpty(user.getEmail())) {
                List<String> recipients = new ArrayList<String>();
                recipients.add(user.getEmail());
                try {
                    sendEmails(oldDeveloper, createdDeveloper != null ? createdDeveloper : newDeveloper,
                            productIdsToMove, splitException, recipients);
                } catch (IOException | MessagingException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.warn("The user " + user.getUsername()
                    + " does not have a configured email address so no email will be sent.");
            }
        }
        LOGGER.info("********* Completed the Split Developer job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private DeveloperDTO splitDeveloper(DeveloperDTO oldDeveloper, DeveloperDTO developerToCreate,
            List<Long> productIdsToMove) throws JsonProcessingException, EntityCreationException,
            EntityRetrievalException, Exception {
        LOGGER.info("Creating new developer " + developerToCreate.getName());
        DeveloperDTO createdDeveloper = devManager.create(developerToCreate);

        // re-assign products to the new developer
        // log activity for all listings whose ID will have changed
        Date splitDate = new Date();
        for (Long productIdToMove : productIdsToMove) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(productIdToMove);
            LOGGER.info("Found " + affectedListings.size() + " affected listings");

            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            Map<Long, CertifiedProductSearchDetails> beforeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
            List<Future<CertifiedProductSearchDetails>> beforeListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings);
            for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                beforeListingDetails.put(details.getId(), details);
            }

            // move the product to be owned by the new developer
            ProductDTO productToMove = productManager.getById(productIdToMove);
            if (productToMove.getOwner().getId().longValue() != oldDeveloper.getId().longValue()) {
                throw new AccessDeniedException("The product " + productToMove.getName()
                    + " is not owned by " + oldDeveloper.getName());
            }
            productToMove.getOwner().setId(createdDeveloper.getId());
            ProductOwnerDTO newOwner = new ProductOwnerDTO();
            newOwner.setProductId(productToMove.getId());
            newOwner.setDeveloper(oldDeveloper);
            newOwner.setTransferDate(splitDate.getTime());
            productToMove.getOwnerHistory().add(newOwner);
            LOGGER.info("Moving product " + productToMove.getName());
            productManager.update(productToMove);

            // get the listing details again - this time they will have the new developer code
            List<Future<CertifiedProductSearchDetails>> afterListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings);
            for (Future<CertifiedProductSearchDetails> future : afterListingFutures) {
                CertifiedProductSearchDetails afterListing = future.get();
                LOGGER.info("Complete retrieving details for id: " + afterListing.getId());
                CertifiedProductSearchDetails beforeListing = beforeListingDetails.get(afterListing.getId());
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                        "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                        afterListing);
            }
        }

        LOGGER.info("Logging developer split activity.");
        DeveloperDTO origDeveloper = devManager.getById(oldDeveloper.getId());
        DeveloperDTO afterDeveloper = devManager.getById(createdDeveloper.getId());
        List<DeveloperDTO> splitDevelopers = new ArrayList<DeveloperDTO>();
        splitDevelopers.add(origDeveloper);
        splitDevelopers.add(afterDeveloper);
        activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                "Split developer " + origDeveloper.getName() + " into " + origDeveloper.getName()
                        + " and " + afterDeveloper.getName(),
                origDeveloper, splitDevelopers);
        return afterDeveloper;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting pre-split details for affected listing " + currListing.getChplProductNumber());
                futures.add(schedulerCertifiedProductSearchDetailsAsync.getCertifiedProductDetail(
                        currListing.getId(), cpdManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private void sendEmails(DeveloperDTO oldDeveloper, DeveloperDTO newDeveloper, List<Long> productIds,
            Exception splitException, List<String> recipients)
            throws IOException, AddressException, MessagingException {

        String subject = getSubject(splitException == null);
        String htmlMessage = "";
        if (splitException == null) {
            htmlMessage = createHtmlEmailBodySuccess(oldDeveloper, newDeveloper, productIds);
        } else {
            String[] errorEmailRecipients = env.getProperty("splitDeveloperErrorEmailRecipients").split(",");
            for (int i = 0; i < errorEmailRecipients.length; i++) {
                recipients.add(errorEmailRecipients[i].trim());
            }
            htmlMessage = createHtmlEmailBodyFailure(oldDeveloper, newDeveloper, splitException);
        }

        for (String emailAddress : recipients) {
            try {
                sendEmail(emailAddress, subject, htmlMessage);
            } catch (Exception ex) {
                LOGGER.error("Could not send message to " + emailAddress, ex);
            }
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws MessagingException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .htmlFooter()
                .sendEmail();
    }

    private String getSubject(boolean success) {
        if (success) {
            return env.getProperty("splitDeveloper.success.emailSubject");
        } else {
            return env.getProperty("splitDeveloper.failed.emailSubject");
        }
    }

    private String createHtmlEmailBodySuccess(DeveloperDTO oldDeveloper, DeveloperDTO createdDeveloper,
            List<Long> productIds) {
        List<ProductDTO> products = new ArrayList<ProductDTO>(productIds.size());
        for (Long productId : productIds) {
            try {
                ProductDTO product = productManager.getById(productId);
                products.add(product);
            } catch (EntityRetrievalException ex) {
                LOGGER.warn("No product found with ID " + productId, ex);
            }
        }

        String htmlMessage = String.format("<p>The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> has been "
                + "created. It was split from <a href=\"%s/#/organizations/developers/%d\">%s</a> and has had the following "
                + "products assigned to it: "
                + "<ul>",
                env.getProperty("chplUrlBegin"), // root of URL
                createdDeveloper.getId(),
                createdDeveloper.getName(),
                env.getProperty("chplUrlBegin"),
                oldDeveloper.getId(),
                oldDeveloper.getName());
        for (ProductDTO product : products) {
            htmlMessage += String.format("<li>%s</li>", product.getName());
        }
        htmlMessage += "</ul>";
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(DeveloperDTO oldDeveloper, DeveloperDTO newDeveloper,
            Exception ex) {
        String htmlMessage = String.format("<p>The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> could not "
                + "be split into a new developer \"%s\".</p>"
                + "<p>The error was: %s</p>",
                env.getProperty("chplUrlBegin"), // root of URL
                oldDeveloper.getId(),
                oldDeveloper.getName(),
                newDeveloper.getName(),
                ex.getMessage());
        return htmlMessage;
    }
}
