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

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "triggerDeveloperBanJobLogger")
public class TriggerDeveloperBanJob implements Job {
    public static final String JOB_NAME = "Trigger Developer Ban Notification";
    public static final String USER = "user";
    public static final String UPDATED_LISTING = "listing";
    public static final String CHANGE_DATE = "changeDate";
    public static final String USER_PROVIDED_REASON = "reason";

    @Autowired
    private ListingSearchService searchService;

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

    @Value("${footer.publicUrl}")
    private String publicFeedbackUrl;

    private CertifiedProductSearchDetails updatedListing;
    private User userPerformingAction;
    private Date listingChangeDate;
    private String userProvidedReason;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Trigger Developer Ban job. *********");

        setJobDataFromMap(jobContext.getMergedJobDataMap());
        String[] recipients = jobContext.getMergedJobDataMap().getString("email").split(",");
        LOGGER.info("Intended recipients are: " + recipients);

        try {
            sendEmails(jobContext, recipients);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Trigger Developer Ban job. *********");
    }

    private void setJobDataFromMap(JobDataMap jobDataMap) {
        updatedListing = (CertifiedProductSearchDetails) jobDataMap.get(UPDATED_LISTING);
        userPerformingAction = (User) jobDataMap.get(USER);
        listingChangeDate = new Date(jobDataMap.getLong(CHANGE_DATE));
        userProvidedReason = jobDataMap.getString(USER_PROVIDED_REASON);
    }

    private void sendEmails(JobExecutionContext jobContext, String[] recipients) throws IOException {
        String subject = String.format(emailSubject, updatedListing.getCurrentStatus().getStatus().getName());
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
        LOGGER.info("Sending email to: " + recipientEmail + " about listing " + updatedListing.getChplProductNumber()
            + (" (ID: " + updatedListing.getId() + ")"));
        chplEmailFactory.emailBuilder()
                .recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBody() {
        String reasonForStatusChange = updatedListing.getCurrentStatus().getReason();
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
        int openSurveillanceNcs = updatedListing.getCountOpenNonconformities();
        int closedSurveillanceNcs = updatedListing.getCountClosedNonconformities();
        int openDirectReviewNcs = 0;
        int closedDirectReviewNcs = 0;
        try {
            ListingSearchResult listingSearchResult = searchService.findListing(updatedListing.getId());
            openDirectReviewNcs = listingSearchResult.getOpenDirectReviewNonConformityCount();
            closedDirectReviewNcs = listingSearchResult.getClosedDirectReviewNonConformityCount();
        } catch (InvalidArgumentsException ex) {
            LOGGER.error("No listing was found with the ID " + updatedListing.getId() + " from the ListingSearchService. The direct review non-conformity counts are unknown.");
        }

        String htmlMessage = String.format(emailBody,
                chplUrlBegin,
                listingDetailsUrl,
                updatedListing.getId(), // for URL to product page
                updatedListing.getChplProductNumber(), // visible link
                updatedListing.getDeveloper().getName(),
                MapUtils.getString(updatedListing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_NAME_KEY),
                Util.getDateFormatter().format(listingChangeDate), // date of change
                userPerformingAction.getFullName(),
                updatedListing.getCurrentStatus().getStatus().getName(), // target status
                Util.getDateFormatter().format(new Date(updatedListing.getCurrentStatus().getEventDate())),
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
            .footer(true)
            .build();
        return formattedHtmlEmailContents;
    }
}
