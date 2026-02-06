package gov.healthit.chpl.scheduler.job.realworldtesting;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlType;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.ErrorMessageUtil;
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

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${contact.acbatlUrl}")
    private String acbatlFeedbackUrl;

    @Value("${surveillance.quarterlyReport.success.subject}")
    private String quarterlyReportSubject;

    @Value("${surveillance.quarterlyReport.failure.subject}")
    private String quarterlyReportFailureSubject;

    @Autowired
    private ErrorMessageUtil msgUtil;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Real World Testing Url Validation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        boolean isJobDataValid = isJobDataValid(jobDataMap);
        if (isJobDataValid) {
            JWTAuthenticatedUser user = (JWTAuthenticatedUser) jobDataMap.get(USER_KEY);
            Long listingId = (Long) jobDataMap.get(LISTING_ID_KEY);
            String url = (String) jobDataMap.get(URL_KEY);
            RealWorldTestingUrlType urlType = RealWorldTestingUrlType.valueOf((String) jobDataMap.get(URL_TYPE_KEY));
            Integer year = (Integer) jobDataMap.get(YEAR_KEY);
            setSecurityContext(user);

            //TODO ensure URL type is RESULTS
            //TODO call AI endpoint, get response or handle error
            //TODO parse results and send email
            sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                                    env.getProperty("surveillance.quarterlyReport.fileError.htmlBody"));

        } else {
            JWTAuthenticatedUser user = (JWTAuthenticatedUser) jobDataMap.get(USER_KEY);
            if (user != null && user.getEmail() != null) {
                sendEmail(user.getEmail(), quarterlyReportFailureSubject,
                        env.getProperty("surveillance.quarterlyReport.badJobData.htmlBody"));
            }
        }
        LOGGER.info("********* Completed the Real World Testing Url Validation job. *********");
    }

    private boolean isJobDataValid(JobDataMap jobDataMap) {
        boolean isValid = true;
        JWTAuthenticatedUser user = (JWTAuthenticatedUser) jobDataMap.get(USER_KEY);
        if (user == null) {
            isValid = false;
            LOGGER.fatal("No user could be found in the job data.");
        }

        Long listingId = (Long) jobDataMap.get(LISTING_ID_KEY);
        if (listingId == null) {
            isValid = false;
            LOGGER.fatal("No listing ID could be found in the job data.");
        }

        String url = (String) jobDataMap.get(URL_KEY);
        if (StringUtils.isEmpty(url)) {
            isValid = false;
            LOGGER.fatal("No URL could be found in the job data.");
        }

        String urlType = (String) jobDataMap.get(URL_TYPE_KEY);
        if (StringUtils.isEmpty(urlType)) {
            isValid = false;
            LOGGER.fatal("No URL Type could be found in the job data.");
        } else {
            RealWorldTestingUrlType urlTypeEnum = RealWorldTestingUrlType.valueOf(urlType);
            if (urlTypeEnum == null || !urlTypeEnum.equals(RealWorldTestingUrlType.RESULTS)) {
                isValid = false;
                LOGGER.fatal("URL Type " + urlType + " is not recognized or supported.");
            }
        }

        String year = (String) jobDataMap.get(YEAR_KEY);
        if (StringUtils.isEmpty(year)) {
            isValid = false;
            LOGGER.fatal("No year could be found in the job data.");
        }
        return isValid;
    }

    private void sendEmail(String recipientEmail, String subject, String htmlContent)  {
        LOGGER.info("Sending email to: " + recipientEmail);

        try {
            chplEmailFactory.emailBuilder()
                    .recipient(recipientEmail)
                    .subject(subject)
                    .htmlMessage(chplHtmlEmailBuilder.initialize()
                            .heading(subject)
                            .paragraph("", htmlContent)
                            .paragraph("", String.format(chplEmailValediction, acbatlFeedbackUrl))
                            .footer(AdminFooter.class)
                            .build())
                    .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error("Could not send email to " + recipientEmail, ex);
        }
    }
}
