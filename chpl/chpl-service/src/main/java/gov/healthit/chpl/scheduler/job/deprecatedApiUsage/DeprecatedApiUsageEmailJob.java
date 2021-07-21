package gov.healthit.chpl.scheduler.job.deprecatedApiUsage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;

public class DeprecatedApiUsageEmailJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("deprecatedApiUsageEmailJobLogger");

    @Autowired
    private DeprecatedApiUsageDao deprecatedApiUsageDao;

    @Autowired
    private Environment env;

    @Value("${deprecatedApiUsage.email.subject}")
    private String deprecatedApiUsageEmailSubject;

    @Value("${deprecatedApiUsage.email.body}")
    private String deprecatedApiUsageEmailBody;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Deprecated Api Usage Email job. *********");

        try {
            List<DeprecatedApiUsage> allDeprecatedApiUsage = deprecatedApiUsageDao.getAllDeprecatedApiUsage();
            LOGGER.info(allDeprecatedApiUsage.size() + " records of deprecated API usage were retrieved.");
            Map<ApiKey, List<DeprecatedApiUsage>> deprecatedApiUsageByApiKey
                = allDeprecatedApiUsage.stream().collect(Collectors.groupingBy(DeprecatedApiUsage::getApiKey));
            LOGGER.info(deprecatedApiUsageByApiKey.keySet().size() + " API Keys accessed deprecated APIs.");

            deprecatedApiUsageByApiKey.keySet()
                .forEach(key -> sendEmailAndDeleteUsageRecords(key, deprecatedApiUsageByApiKey.get(key)));
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the Deprecated Api Usage Email job. *********");
    }

    private void sendEmailAndDeleteUsageRecords(ApiKey apiKey, List<DeprecatedApiUsage> deprecatedApiUsage) {
        LOGGER.info("API Key for " + apiKey.getEmail() + " has used " + deprecatedApiUsage.size() + " deprecated APIs.");
        EmailBuilder emailBuilder = new EmailBuilder(env);
        try {
            emailBuilder.recipient(apiKey.getEmail())
                .subject(deprecatedApiUsageEmailSubject)
                .htmlMessage(createHtmlMessage(apiKey, deprecatedApiUsage))
                .publicHtmlFooter()
                .sendEmail();
            LOGGER.info("Sent email to " + apiKey.getEmail() + ".");
            deprecatedApiUsage.stream().forEach(item -> deleteDeprecatedApiUsage(item));
        } catch (Exception ex) {
            LOGGER.error("Unable to send email to " + apiKey.getEmail() + ". "
                    + "User may not have been notified and database records will not be deleted.", ex);
        }
    }

    private String createHtmlMessage(ApiKey apiKey, List<DeprecatedApiUsage> deprecatedApiUsage) throws IOException {
        List<String> apiUsageHeading  = Stream.of("HTTP Method", "API Endpoint", "Usage Count", "Last Accessed", "Message").collect(Collectors.toList());
        List<List<String>> apiUsageData = new ArrayList<List<String>>();
        deprecatedApiUsage.stream().forEach(api -> apiUsageData.add(createUsageData(api)));
        ChplHtmlEmailBuilder emailBuilder = new ChplHtmlEmailBuilder();
        String htmlMessage = emailBuilder.addHeading("Deprecated API Usage Notification", null)
            .addParagraph("", String.format(deprecatedApiUsageEmailBody, apiKey.getKey()))
            .addTable(apiUsageHeading, apiUsageData)
            .addFooter()
            .build();
        LOGGER.debug("HTML Email being sent to " + apiKey.getEmail() + ": \n" + htmlMessage);
        return htmlMessage;
    }

    private List<String> createUsageData(DeprecatedApiUsage deprecatedApiUsage) {
        return Stream.of(deprecatedApiUsage.getApi().getHttpMethod().name(),
                deprecatedApiUsage.getApi().getApiOperation(),
                deprecatedApiUsage.getCallCount().toString(),
                deprecatedApiUsage.getLastAccessedDate().toString(),
                deprecatedApiUsage.getApi().getChangeDescription()).collect(Collectors.toList());
    }

    private void deleteDeprecatedApiUsage(DeprecatedApiUsage deprecatedApiUsage) {
        try {
            deprecatedApiUsageDao.delete(deprecatedApiUsage.getId());
            LOGGER.info("Deleted deprecated API usage with ID " + deprecatedApiUsage.getId());
        } catch (Exception ex) {
            LOGGER.error("Error deleting deprecated API usage with ID " + deprecatedApiUsage.getId(), ex);
        }
    }
}
