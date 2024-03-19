package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.compliance.directreview.DirectReviewUpdateEmailService;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.util.DateUtil;

@DisallowConcurrentExecution
public class SplitDeveloperJob extends QuartzJob {
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
    private ActivityManager activityManager;

    @Autowired
    private DirectReviewUpdateEmailService directReviewEmailService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder emailBuilder;

    @Value("${internalErrorEmailRecipients}")
    private String internalErrorEmailRecipients;

    private Developer preSplitDeveloper;
    private Developer postSplitDeveloper;
    private Map<Long, CertifiedProductSearchDetails> preSplitListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
    private Map<Long, CertifiedProductSearchDetails> postSplitListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();

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

            preSplitDeveloper = (Developer) jobDataMap.get(OLD_DEVELOPER_KEY);
            Developer newDeveloper = (Developer) jobDataMap.get(NEW_DEVELOPER_KEY);
            List<Long> productIdsToMove = (List<Long>) jobDataMap.get(PRODUCT_IDS_TO_MOVE_KEY);
            Exception splitException = null;
            try {
                postSplitDeveloper = splitDeveloper(newDeveloper, productIdsToMove);
            } catch (Exception e) {
                LOGGER.error("Error completing split of old developer '" + preSplitDeveloper.getName() + "' to new developer '"
                        + newDeveloper.getName() + "'.", e);
                splitException = e;
            }

            if (postSplitDeveloper != null) {
                clearCachesRelatedToDevelopers();
            }

            directReviewEmailService.sendEmail(
                    Arrays.asList(preSplitDeveloper),
                    Arrays.asList(preSplitDeveloper, postSplitDeveloper),
                    preSplitListingDetails, postSplitListingDetails,
                    DirectReviewUpdateEmailService.CONTEXT_DEVELOPER_SPLIT, LOGGER);

            //send email about success/failure of job
            if (!StringUtils.isEmpty(user.getEmail())) {
                List<String> recipients = new ArrayList<String>();
                recipients.add(user.getEmail());
                try {
                    sendJobCompletionEmails(postSplitDeveloper != null ? postSplitDeveloper : newDeveloper,
                            productIdsToMove, splitException, recipients);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            } else {
                LOGGER.warn("The user " + user.getUsername()
                    + " does not have a configured email address so no email will be sent.");
            }
        }
        LOGGER.info("********* Completed the Split Developer job. *********");
    }

    private Developer splitDeveloper(Developer developerToCreate, List<Long> productIdsToMove)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, Exception {
        LOGGER.info("Creating new developer " + developerToCreate.getName());
        Long createdDeveloperId = devManager.create(developerToCreate);

        // re-assign products to the new developer
        // log activity for all listings whose ID will have changed
        Date splitDate = new Date();
        for (Long productIdToMove : productIdsToMove) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(productIdToMove);
            LOGGER.info("Found " + affectedListings.size() + " affected listings");

            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            List<Future<CertifiedProductSearchDetails>> beforeListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings, true);
            for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                preSplitListingDetails.put(details.getId(), details);
            }

            // move the product to be owned by the new developer
            Product productToMove = productManager.getById(productIdToMove);
            if (productToMove.getOwner().getId().longValue() != preSplitDeveloper.getId().longValue()) {
                throw new AccessDeniedException("The product " + productToMove.getName()
                    + " is not owned by " + preSplitDeveloper.getName());
            }
            productToMove.getOwner().setId(createdDeveloperId);
            ProductOwner newOwner = new ProductOwner();
            newOwner.setDeveloper(preSplitDeveloper);
            newOwner.setTransferDay(DateUtil.toLocalDate(splitDate.getTime()));
            productToMove.getOwnerHistory().add(newOwner);
            LOGGER.info("Moving product " + productToMove.getName());
            productManager.update(productToMove);

            // get the listing details again - this time they will have the new developer code
            List<Future<CertifiedProductSearchDetails>> afterListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings, false);
            for (Future<CertifiedProductSearchDetails> future : afterListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                postSplitListingDetails.put(details.getId(), details);
            }
        }

        LOGGER.info("Logging listing split activity.");
        for (Long id : postSplitListingDetails.keySet()) {
            CertifiedProductSearchDetails preSplitListing = preSplitListingDetails.get(id);
            CertifiedProductSearchDetails postSplitListing = postSplitListingDetails.get(id);
            if (!StringUtils.equals(preSplitListing.getChplProductNumber(), postSplitListing.getChplProductNumber())) {
                //listing activity should only be recorded if the CHPL product number of the listing changed
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, preSplitListing.getId(),
                    "Updated certified product " + postSplitListing.getChplProductNumber() + ".", preSplitListing,
                    postSplitListing);
            }
        }

        LOGGER.info("Logging developer split activity.");
        Developer origDeveloper = devManager.getById(preSplitDeveloper.getId());
        Developer afterDeveloper = devManager.getById(createdDeveloperId);
        List<Developer> splitDevelopers = new ArrayList<Developer>();
        splitDevelopers.add(origDeveloper);
        splitDevelopers.add(afterDeveloper);
        activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                "Split developer " + origDeveloper.getName() + " into " + origDeveloper.getName()
                        + " and " + afterDeveloper.getName(),
                origDeveloper, splitDevelopers);
        return afterDeveloper;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            List<CertifiedProductDetailsDTO> listings, boolean fromCache) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting details for affected listing " + currListing.getChplProductNumber());
                futures.add(new AsyncResult<CertifiedProductSearchDetails>(
                        getDetails(currListing.getId(), fromCache)));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private CertifiedProductSearchDetails getDetails(Long listingId, boolean fromCache) throws EntityRetrievalException {
        if (fromCache) {
            return cpdManager.getCertifiedProductDetails(listingId);
        } else {
            return cpdManager.getCertifiedProductDetailsNoCache(listingId);
        }
    }

    @ListingSearchCacheRefresh
    private void clearCachesRelatedToDevelopers() {
        cacheManager.getCache(CacheNames.ALL_DEVELOPERS).invalidate();
        cacheManager.getCache(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED).invalidate();
        cacheManager.getCache(CacheNames.COLLECTIONS_DEVELOPERS).invalidate();
        cacheManager.getCache(CacheNames.COLLECTIONS_LISTINGS).invalidate();
        cacheManager.getCache(CacheNames.GET_DECERTIFIED_DEVELOPERS).invalidate();
    }

    private void sendJobCompletionEmails(Developer newDeveloper, List<Long> productIds,
            Exception splitException, List<String> recipients) throws IOException {

        String subject = getSubject(splitException == null);
        String htmlMessage = "";
        if (splitException == null) {
            htmlMessage = createHtmlEmailBodySuccess(subject, newDeveloper, productIds);
        } else {
            String[] errorEmailRecipients = internalErrorEmailRecipients.split(",");
            for (int i = 0; i < errorEmailRecipients.length; i++) {
                recipients.add(errorEmailRecipients[i].trim());
            }
            htmlMessage = createHtmlEmailBodyFailure(subject, newDeveloper, splitException);
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
            throws EmailNotSentException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        chplEmailFactory.emailBuilder().recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String getSubject(boolean success) {
        if (success) {
            return env.getProperty("splitDeveloper.success.emailSubject");
        } else {
            return env.getProperty("splitDeveloper.failed.emailSubject");
        }
    }

    private String createHtmlEmailBodySuccess(String title, Developer createdDeveloper, List<Long> productIds) {
        List<Product> products = new ArrayList<Product>(productIds.size());
        for (Long productId : productIds) {
            try {
                Product product = productManager.getById(productId);
                products.add(product);
            } catch (EntityRetrievalException ex) {
                LOGGER.warn("No product found with ID " + productId, ex);
            }
        }

        String summaryText = String.format("<p>The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> has been "
                + "created. It was split from <a href=\"%s/#/organizations/developers/%d\">%s</a> and has had the following "
                + "products assigned to it: ",
                env.getProperty("chplUrlBegin"), // root of URL
                createdDeveloper.getId(),
                createdDeveloper.getName(),
                env.getProperty("chplUrlBegin"),
                preSplitDeveloper.getId(),
                preSplitDeveloper.getName());

        String productList = "<ul>";
        for (Product product : products) {
            productList += String.format("<li>%s</li>", product.getName());
        }
        productList += "</ul>";

        String htmlMessage = emailBuilder.initialize()
                .heading(title)
                .paragraph(null, summaryText)
                .paragraph(null, productList)
                .footer(AdminFooter.class)
                .build();
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(String title, Developer newDeveloper, Exception ex) {
        String summaryText = String.format("The Developer <a href=\"%s/#/organizations/developers/%d\">%s</a> could not "
                + "be split into a new developer \"%s\".",
                env.getProperty("chplUrlBegin"), // root of URL
                preSplitDeveloper.getId(),
                preSplitDeveloper.getName(),
                newDeveloper.getName());

        String exceptionMessage = "";
        if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            exceptionMessage = validationEx.getErrorMessages().stream()
                    .collect(Collectors.joining("<br />"));
        } else {
            exceptionMessage = ex.getMessage();
        }

        String htmlMessage = emailBuilder.initialize()
                .heading(title)
                .paragraph(null, summaryText)
                .paragraph(null, String.format("Reason for failure: %s", exceptionMessage))
                .footer(AdminFooter.class)
                .build();

        return htmlMessage;
    }
}
