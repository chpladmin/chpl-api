package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.DeveloperDAO;
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
import gov.healthit.chpl.service.DirectReviewUpdateEmailService;
import gov.healthit.chpl.util.EmailBuilder;
import net.sf.ehcache.CacheManager;

public class MergeDeveloperJob implements Job {
    public static final String JOB_NAME = "mergeDeveloperJob";
    public static final String OLD_DEVELOPERS_KEY = "oldDevelopers";
    public static final String NEW_DEVELOPER_KEY = "newDeveloper";
    public static final String PRODUCT_IDS_TO_MOVE_KEY = "productIdsToMove";
    public static final String USER_KEY = "user";
    private static final Logger LOGGER = LogManager.getLogger("mergeDeveloperJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private DeveloperDAO devDao;

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

    @Autowired
    private DirectReviewUpdateEmailService directReviewEmailService;

    private List<DeveloperDTO> preMergeDevelopers;
    private DeveloperDTO postMergeDeveloper;
    private Map<Long, CertifiedProductSearchDetails> preMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
    private Map<Long, CertifiedProductSearchDetails> postMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Merge Developer job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);

            preMergeDevelopers = (List<DeveloperDTO>) jobDataMap.get(OLD_DEVELOPERS_KEY);
            DeveloperDTO newDeveloper = (DeveloperDTO) jobDataMap.get(NEW_DEVELOPER_KEY);
            Exception mergeException = null;
            try {
                //merge within transaction so changes will be rolled back
                TransactionalDeveloperMerge merger = new TransactionalDeveloperMerge();
                postMergeDeveloper = merger.merge(preMergeDevelopers, newDeveloper);
            } catch (Exception e) {
                LOGGER.error("Error completing merge of developers '"
                        + StringUtils.join(preMergeDevelopers.stream()
                            .map(DeveloperDTO::getName)
                            .collect(Collectors.toList()), ",")
                        + "' to new developer '"
                        + newDeveloper.getName() + "'.", e);
                mergeException = e;
            }

            if (postMergeDeveloper != null) {
                CacheManager.getInstance().clearAll();
            }

            //send email about success/failure of job
            if (!StringUtils.isEmpty(user.getEmail())) {
                List<String> recipients = new ArrayList<String>();
                recipients.add(user.getEmail());
                try {
                    sendJobCompletionEmails(postMergeDeveloper != null ? postMergeDeveloper : newDeveloper,
                            preMergeDevelopers, mergeException, recipients);
                } catch (IOException | MessagingException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.warn("The user " + user.getUsername()
                    + " does not have a configured email address so no email will be sent.");
            }
        }
        LOGGER.info("********* Completed the Merge Developer job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser mergeUser = new JWTAuthenticatedUser();
        mergeUser.setFullName(user.getFullName());
        mergeUser.setId(user.getId());
        mergeUser.setFriendlyName(user.getFriendlyName());
        mergeUser.setSubjectName(user.getUsername());
        mergeUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(mergeUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting details for affected listing " + currListing.getChplProductNumber());
                futures.add(schedulerCertifiedProductSearchDetailsAsync.getCertifiedProductDetail(
                        currListing.getId(), cpdManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private void sendJobCompletionEmails(DeveloperDTO newDeveloper, List<DeveloperDTO> oldDevelopers,
            Exception mergeException, List<String> recipients) throws IOException, AddressException, MessagingException {

        String subject = getSubject(mergeException == null);
        String htmlMessage = "";
        if (mergeException == null) {
            htmlMessage = createHtmlEmailBodySuccess(newDeveloper, oldDevelopers);
        } else {
            String[] errorEmailRecipients = env.getProperty("mergeDeveloperErrorEmailRecipients").split(",");
            for (int i = 0; i < errorEmailRecipients.length; i++) {
                recipients.add(errorEmailRecipients[i].trim());
            }
            htmlMessage = createHtmlEmailBodyFailure(oldDevelopers, mergeException);
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
                .acbAtlHtmlFooter()
                .sendEmail();
    }

    private String getSubject(boolean success) {
        if (success) {
            return env.getProperty("mergeDeveloper.success.emailSubject");
        } else {
            return env.getProperty("mergeDeveloper.failed.emailSubject");
        }
    }

    private String createHtmlEmailBodySuccess(DeveloperDTO createdDeveloper, List<DeveloperDTO> preMergeDevelopers) {
        String htmlMessage = String.format("<p>The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> has been "
                + "created. It was merged from the following developers: </p>"
                + "<ul>",
                env.getProperty("chplUrlBegin"), // root of URL
                createdDeveloper.getId(),
                createdDeveloper.getName());
        for (DeveloperDTO dev : preMergeDevelopers) {
            htmlMessage += String.format("<li><a href=\"%s/#/organizations/developers/%d\">%s</a></li>",
                    env.getProperty("chplUrlBegin"),
                    dev.getId(),
                    dev.getName());
        }
        htmlMessage += "</ul>";
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(List<DeveloperDTO> preMergeDevelopers, Exception ex) {
        String htmlMessage = "The developers <ul> ";
        for (DeveloperDTO dev : preMergeDevelopers) {
            htmlMessage += String.format("<li><a href=\"%s/#/organizations/developers/%d\">%s</a></li>",
                    env.getProperty("chplUrlBegin"),
                    dev.getId(),
                    dev.getName());
        }
        htmlMessage += String.format(
                "</ul>"
                + " could not be merged into a new developer. The error was: %s",
                ex.getMessage());
        return htmlMessage;
    }

    private class TransactionalDeveloperMerge {

        @Transactional
        public DeveloperDTO merge(List<DeveloperDTO> beforeDevelopers, DeveloperDTO developerToCreate)
                throws JsonProcessingException, EntityCreationException, EntityRetrievalException, Exception {
            List<Long> developerIdsToMerge = beforeDevelopers.stream()
                    .map(DeveloperDTO::getId)
                    .collect(Collectors.toList());
            LOGGER.info("Creating new developer " + developerToCreate.getName());
            DeveloperDTO createdDeveloper = devManager.create(developerToCreate);

            Map<Long, CertifiedProductSearchDetails> preMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
            Map<Long, CertifiedProductSearchDetails> postMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
            // search for any products assigned to the list of developers passed in
            List<ProductDTO> developerProducts = productManager.getByDevelopers(developerIdsToMerge);
            for (ProductDTO product : developerProducts) {
                List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(product.getId());
                LOGGER.info("Found " + affectedListings.size() + " affected listings under product " + product.getName());
                // need to get details for affected listings now before the product is re-assigned
                // so that any listings with a generated new-style CHPL ID have the old developer code
                List<Future<CertifiedProductSearchDetails>> beforeListingFutures
                    = getCertifiedProductSearchDetailsFutures(affectedListings);
                for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                    CertifiedProductSearchDetails details = future.get();
                    LOGGER.info("Complete retrieving details for id: " + details.getId());
                    preMergeListingDetails.put(details.getId(), details);
                }

                // add an item to the ownership history of each product
                ProductOwnerDTO historyToAdd = new ProductOwnerDTO();
                historyToAdd.setProductId(product.getId());
                DeveloperDTO prevOwner = new DeveloperDTO();
                prevOwner.setId(product.getOwner().getId());
                historyToAdd.setDeveloper(prevOwner);
                historyToAdd.setTransferDate(System.currentTimeMillis());
                product.getOwnerHistory().add(historyToAdd);
                // reassign those products to the new developer
                product.getOwner().setId(createdDeveloper.getId());
                productManager.update(product);

                // get the listing details again - this time they will have the new developer code
                List<Future<CertifiedProductSearchDetails>> afterListingFutures
                    = getCertifiedProductSearchDetailsFutures(affectedListings);
                for (Future<CertifiedProductSearchDetails> future : afterListingFutures) {
                    CertifiedProductSearchDetails details = future.get();
                    LOGGER.info("Complete retrieving details for id: " + details.getId());
                    postMergeListingDetails.put(details.getId(), details);
                }
            }

            // - mark the passed in developers as deleted
            for (Long developerId : developerIdsToMerge) {
                devDao.delete(developerId);
            }

            LOGGER.info("Logging listing activity for developer merge.");
            for (Long id : postMergeListingDetails.keySet()) {
                CertifiedProductSearchDetails preMergeListing = preMergeListingDetails.get(id);
                CertifiedProductSearchDetails postMergeListing = postMergeListingDetails.get(id);
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, preMergeListing.getId(),
                        "Updated certified product " + postMergeListing.getChplProductNumber() + ".", preMergeListing,
                        postMergeListing);
            }

            directReviewEmailService.sendEmail(beforeDevelopers, Arrays.asList(createdDeveloper),
                    preMergeListingDetails, postMergeListingDetails);

            LOGGER.info("Logging developer merge activity.");
            DeveloperDTO afterDeveloper = devManager.getById(createdDeveloper.getId());
            String beforeDevNames = String.join(",",
                    beforeDevelopers.stream().map(DeveloperDTO::getName).collect(Collectors.toList()));
            activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                    "Merged developers " + beforeDevNames + " into " + afterDeveloper.getName(),
                    beforeDevelopers, afterDeveloper);

            return createdDeveloper;
        }
    }
}
