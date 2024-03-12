package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "triggerDeveloperBanJobLogger")
public class TriggerDeveloperBanJob implements Job {
    public static final String JOB_NAME = "Trigger Developer Ban Notification";
    public static final String USER = "user";
    public static final String LISTING_ID = "listingId";
    public static final String CHANGE_DATE = "changeDate";
    public static final String USER_PROVIDED_REASON = "reason";

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private DirectReviewSearchService drSearchService;

    @Autowired
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${triggerDeveloperBan.subject}")
    private String emailSubject;

    @Value("${triggerDeveloperBan.body}")
    private String emailBody;

    @Value("${triggerDeveloperBan.directReviewsNotAvailable}")
    private String drsNotAvailableEmailBody;

    @Value("${chplUrlBegin}")
    private String chplUrlBegin;

    @Value("${listingDetailsUrl}")
    private String listingDetailsUrl;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${contact.publicUrl}")
    private String publicFeedbackUrl;

    private JWTAuthenticatedUser userPerformingAction;
    private Long listingId;
    private CertifiedProductSearchDetails listing;
    private Date listingChangeDate;
    private String userProvidedReason;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Trigger Developer Ban job. *********");

        setJobDataFromMap(jobContext.getMergedJobDataMap());
        String recipientEmails = jobContext.getMergedJobDataMap().getString("email");
        LOGGER.info("Intended job recipients are: " + recipientEmails);
        String[] recipients = null;
        if (!StringUtils.isEmpty(recipientEmails)) {
            recipients = recipientEmails.split(",");
            LOGGER.info("Parsed " + recipients.length + " job recipients from job data.");
        } else {
            LOGGER.error("No recipients are defined for the Trigger Developer Ban Job!");
            recipients = new String[0];
        }

        try {
            sendEmails(jobContext, recipients);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Trigger Developer Ban job. *********");
    }

    private void setJobDataFromMap(JobDataMap jobDataMap) {
        listingId = (Long) jobDataMap.get(LISTING_ID);
        try {
            listing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            if (listing == null) {
                throw new EntityRetrievalException();
            }
        } catch (EntityRetrievalException ex) {
            LOGGER.error("No listing found with ID " + listingId + ". Job cannot proceed.");
            throw new RuntimeException();
        }

        userPerformingAction = (JWTAuthenticatedUser) jobDataMap.get(USER);
        listingChangeDate = new Date(jobDataMap.getLong(CHANGE_DATE));
        userProvidedReason = jobDataMap.getString(USER_PROVIDED_REASON);
    }

    private void sendEmails(JobExecutionContext jobContext, String[] recipients) throws IOException {
        String subject = String.format(emailSubject, listing.getCurrentStatus().getStatus().getName());
        String htmlMessage = createHtmlEmailBody();

        List<String> emailAddresses = Arrays.asList(recipients);
        for (String emailAddress : emailAddresses) {
            try {
                if (EmailValidator.getInstance().isValid(emailAddress)) {
                    sendEmail(emailAddress, subject, htmlMessage);
                } else {
                    LOGGER.error("Detected invalid email address '" + emailAddress + "'. Not sending that email.");
                }
            } catch (Exception ex) {
                LOGGER.error("Could not send message to " + emailAddress, ex);
            }
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws EmailNotSentException {
        LOGGER.info("Sending email to: " + recipientEmail + " about listing " + listing.getChplProductNumber()
            + (" (ID: " + listing.getId() + ")"));
        chplEmailFactory.emailBuilder()
                .recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBody() {
        String reasonForStatusChange = listing.getCurrentStatus().getReason();
        if (StringUtils.isEmpty(reasonForStatusChange)) {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> This field is blank";
        } else {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> \""
                    + reasonForStatusChange + "\"";
        }
        String reasonForListingChange = userProvidedReason;
        if (StringUtils.isEmpty(reasonForListingChange)) {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> This field is blank";
        } else {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> \""
                    + reasonForListingChange + "\"";
        }
        long openSurveillanceNcs = 0, closedSurveillanceNcs = 0;
        int openDirectReviewNcs = 0, closedDirectReviewNcs = 0;
        try {
            openDirectReviewNcs = listingSearchService.findListing(listingId).getOpenDirectReviewNonConformityCount();
            closedDirectReviewNcs = listingSearchService.findListing(listingId).getClosedDirectReviewNonConformityCount();
            openSurveillanceNcs = listing.getCountOpenNonconformities();
            closedSurveillanceNcs = listing.getCountClosedNonconformities();
        } catch (InvalidArgumentsException ex) {
            LOGGER.error("No listing was found with the ID " + listing.getId() + " from the ListingSearchService. The direct review non-conformity counts are unknown.");
        }

        String htmlMessage = String.format(emailBody,
                chplUrlBegin,
                listingDetailsUrl,
                listing.getId(), // for URL to product page
                listing.getChplProductNumber(), // visible link
                listing.getDeveloper().getName(),
                MapUtils.getString(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY),
                Util.getDateFormatter().format(listingChangeDate), // date of change
                userPerformingAction.getFullName(),
                listing.getCurrentStatus().getStatus().getName(), // target status
                Util.getDateFormatter().format(new Date(listing.getCurrentStatus().getEventDate())),
                reasonForStatusChange, // reason for change
                reasonForListingChange, // reason for change
                (openSurveillanceNcs != 1 ? "were" : "was"), openSurveillanceNcs, (openSurveillanceNcs != 1 ? "ies" : "y"), // formatted counts of open
                closedSurveillanceNcs, (closedSurveillanceNcs != 1 ? "ies" : "y"), // and closed surveillance nonconformities, with English word endings
                (openDirectReviewNcs != 1 ? "were" : "was"), openDirectReviewNcs, (openDirectReviewNcs != 1 ? "ies" : "y"), // formatted counts of open
                closedDirectReviewNcs, (closedDirectReviewNcs != 1 ? "ies" : "y")); // and closed direct review nonconformities, with English word endings

        if (!drSearchService.doesCacheHaveAnyOkData()) {
            htmlMessage += drsNotAvailableEmailBody;
        }

        String formattedHtmlEmailContents = htmlEmailBuilder.initialize()
            .heading("Review Activity for Developer Ban")
            .paragraph(null, htmlMessage)
            .paragraph("", String.format(chplEmailValediction, publicFeedbackUrl))
            .footer(AdminFooter.class)
            .build();
        return formattedHtmlEmailContents;
    }
}
