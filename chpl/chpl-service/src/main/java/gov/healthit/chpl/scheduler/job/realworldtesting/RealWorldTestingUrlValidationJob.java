package gov.healthit.chpl.scheduler.job.realworldtesting;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.astpai.AmazonTokenResponse;
import gov.healthit.chpl.astpai.AstpAiAuthenticationService;
import gov.healthit.chpl.astpai.AstpAiQueryService;
import gov.healthit.chpl.astpai.AstpAiRequestFailedException;
import gov.healthit.chpl.astpai.UrlValidationRequest;
import gov.healthit.chpl.astpai.UrlValidationResponse;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlType;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "realWorldTestingUrlValidationJobLogger")
public class RealWorldTestingUrlValidationJob extends QuartzJob {
    public static final String JOB_NAME = "realWorldTestingUrlValidationJob";
    public static final String LISTING_ID_KEY = "listingId";
    public static final String URL_KEY = "url";
    public static final String URL_TYPE_KEY = "urlType";
    public static final String YEAR_KEY = "year";
    public static final String USER_KEY = "user";
    private static final Integer MAX_SEARCH_DEPTH = 5;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${contact.acbatlUrl}")
    private String acbatlFeedbackUrl;

    @Value("${rwtResults.validation.subject}")
    private String emailSubject;

    @Value("${rwtResults.validation.body}")
    private String emailBody;

    @Value("${rwtResults.validation.failure.subject}")
    private String failureEmailSubject;

    @Value("${rwtResults.validation.failure.body}")
    private String failureEmailBody;

    @Autowired
    private AstpAiAuthenticationService aiAuthService;

    @Autowired
    private AstpAiQueryService aiQueryService;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private JWTAuthenticatedUser user;
    private String url;
    private Long listingId;
    private Integer year;
    private RealWorldTestingUrlType urlType;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Real World Testing Url Validation job. *********");
        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        parseJobData(jobDataMap);
        if (isJobDataValid()) {
            setSecurityContext(user);

            LOGGER.info("Validating URL " + url + " for listing " + listingId + " and year " + year);
            //authenticate
            AmazonTokenResponse token = null;
            try {
                token = aiAuthService.authenticate();
            } catch (AstpAiRequestFailedException ex) {
                LOGGER.error("Unable to authenticate with ASTP-AI", ex);
                sendErrorEmail(user.getEmail(), "Unable to authenticate with ASTP-AI");
                return;
            }
            LOGGER.info("Successfully authenticated with the ASTP-AI application");
            //call AI endpoint, get response or handle error
            UrlValidationResponse aiResponse = null;
            if (token != null) {
                try {
                    LOGGER.info("Requesting RWT URL Validation from the ASTP-AI application");
                    aiResponse = aiQueryService.getRwtResultsUrlValidationResponse(token.getAccessToken(), UrlValidationRequest.builder()
                        .chplProductNumber(getChplProductNumber())
                        .url(url)
                        .maxDepth(MAX_SEARCH_DEPTH)
                        .targetYear(year)
                        .build());
                } catch (AstpAiRequestFailedException ex) {
                    LOGGER.error("Unable to query ASTP-AI endpoint", ex);
                    sendErrorEmail(user.getEmail(), "Unable to query ASTP-AI endpoint: " + ex.getMessage());
                    return;
                } catch (Exception ex) {
                    LOGGER.error("Unexpected error querying ASTP-AI endpoint", ex);
                    sendErrorEmail(user.getEmail(), "Unexpected error querying ASTP-AI endpoint: " + ex.getMessage());
                    return;
                }
            } else {
                LOGGER.error("Unable to authenticate with ASTP-AI");
                sendErrorEmail(user.getEmail(), "Unable to authenticate with ASTP-AI");
                return;
            }
            LOGGER.info("Received validation results. Emailing " + user.getEmail());
            //parse results and send email
            sendResultsEmail(user.getEmail(), aiResponse);
        } else {
            LOGGER.error("Invalid inputs to job.");
            //invalid inputs in the job data
            user = (JWTAuthenticatedUser) jobDataMap.get(USER_KEY);
            if (user != null && user.getEmail() != null) {
                sendErrorEmail(user.getEmail(), "Invalid inputs for RWT URL Validation");
            } else {
                LOGGER.fatal("Invalid inputs to job and no user was found to email.");
            }
        }
        LOGGER.info("********* Completed the Real World Testing Url Validation job. *********");
    }

    private void parseJobData(JobDataMap jobDataMap) {
        user = (JWTAuthenticatedUser) jobDataMap.get(USER_KEY);
        listingId = (Long) jobDataMap.get(LISTING_ID_KEY);
        url = (String) jobDataMap.get(URL_KEY);
        urlType = RealWorldTestingUrlType.valueOf((String) jobDataMap.get(URL_TYPE_KEY));
        year = (Integer) jobDataMap.get(YEAR_KEY);
    }

    private boolean isJobDataValid() {
        boolean isValid = true;
        if (user == null) {
            isValid = false;
            LOGGER.fatal("No user could be found in the job data.");
        }

        if (listingId == null) {
            isValid = false;
            LOGGER.fatal("No listing ID could be found in the job data.");
        } else {
            ListingSearchResult listing = null;
            try {
                listing = listingSearchService.findListing(listingId);
            } catch (InvalidArgumentsException ex) {
                LOGGER.fatal("Invalid listing ID " + listingId + " found in the job data.", ex);
                isValid = false;
            }
            if (listing == null) {
                isValid = false;
            }
        }

        if (StringUtils.isEmpty(url)) {
            isValid = false;
            LOGGER.fatal("No URL could be found in the job data.");
        }

        if (urlType == null) {
            isValid = false;
            LOGGER.fatal("A valid URL Type was not found in the job data.");
        }

        if (year == null) {
            isValid = false;
            LOGGER.fatal("No year could be found in the job data.");
        }
        return isValid;
    }

    private String getChplProductNumber() {
        ListingSearchResult result = null;
        try {
            result = listingSearchService.findListing(listingId);
        } catch (InvalidArgumentsException ex) {
            LOGGER.error("No listing with ID " + listingId + " was found.");
        }
        if (result == null) {
            return "";
        }
        return result.getChplProductNumber();
    }

    private void sendResultsEmail(String recipientEmail, UrlValidationResponse results)  {
        LOGGER.info("Sending email to: " + recipientEmail);
        String resultsHtml = createResultsHtml(results);
        try {
            chplEmailFactory.emailBuilder()
                    .recipient(recipientEmail)
                    .subject(emailSubject)
                    .htmlMessage(chplHtmlEmailBuilder.initialize()
                            .heading(emailSubject)
                            .paragraph("", String.format(emailBody, url, getChplProductNumber(), year + "", resultsHtml))
                            .paragraph("", String.format(chplEmailValediction, acbatlFeedbackUrl))
                            .footer(AdminFooter.class)
                            .build())
                    .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error("Could not send email to " + recipientEmail, ex);
        }
    }

    private String createResultsHtml(UrlValidationResponse results) {
        StringBuffer buf = new StringBuffer();
        if (!StringUtils.isEmpty(results.getError())) {
            buf.append("<p><b>Error: </b>" + results.getError() + "</p>");
        }
        if (results.getDocument() != null) {
            buf.append("<h3>Document Discovery</h3><ul>");
            if (!StringUtils.isEmpty(results.getDocument().getUrl())) {
                buf.append("<li>Document URL: " + results.getDocument().getUrl() + "</li>");
            }
            if (!StringUtils.isEmpty(results.getDocument().getConfidence())) {
                buf.append("<li>Confidence: " + results.getDocument().getConfidence() + "%</li>");
            }
            buf.append("</ul>");
        }
        if (results.getValidation() != null) {
            buf.append("<h3>Validation Results</h3><ul>");
            if (!StringUtils.isEmpty(results.getValidation().getCompletenessScore())) {
                buf.append("<li>Completeness Score: " + results.getValidation().getCompletenessScore() + "%</li>");
            }
            if (!StringUtils.isEmpty(results.getValidation().getSummary())) {
                buf.append("<li>Validation Summary: " + results.getValidation().getSummary() + "</li>");
            }
            buf.append("</ul>");
        }
        return buf.toString();
    }

    private void sendErrorEmail(String recipientEmail, String errorMessage)  {
        LOGGER.info("Sending email to: " + recipientEmail);

        try {
            chplEmailFactory.emailBuilder()
                    .recipient(recipientEmail)
                    .subject(failureEmailSubject)
                    .htmlMessage(chplHtmlEmailBuilder.initialize()
                            .heading(failureEmailSubject)
                            .paragraph("", String.format(failureEmailBody, url, listingId + "", year + "", errorMessage))
                            .paragraph("", String.format(chplEmailValediction, acbatlFeedbackUrl))
                            .footer(AdminFooter.class)
                            .build())
                    .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error("Could not send email to " + recipientEmail, ex);
        }
    }
}
