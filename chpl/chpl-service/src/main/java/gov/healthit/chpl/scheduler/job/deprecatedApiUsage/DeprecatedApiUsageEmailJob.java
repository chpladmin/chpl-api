package gov.healthit.chpl.scheduler.job.deprecatedApiUsage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
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
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseFieldApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseFieldApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.util.DateUtil;

public class DeprecatedApiUsageEmailJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("deprecatedApiUsageEmailJobLogger");

    @Autowired
    private DeprecatedApiUsageDao deprecatedApiUsageDao;

    @Autowired
    private DeprecatedResponseFieldApiUsageDao deprecatedResponseFieldApiUsageDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private Environment env;

    @Value("${deprecatedApiUsage.email.subject}")
    private String deprecatedApiUsageEmailSubject;

    @Value("${deprecatedApiUsage.email.heading}")
    private String deprecatedApiUsageEmailHeading;

    @Value("${chpl.email.greeting}")
    private String chplEmailGreeting;

    @Value("${deprecatedApiUsage.email.body}")
    private String deprecatedApiUsageEmailBody;

    @Value("${deprecatedApiUsage.email.deprecatedApiParagraph}")
    private String deprecatedApiParagraph;

    @Value("${deprecatedApiUsage.email.deprecatedResponseFieldParagraph}")
    private String deprecatedResponseFieldParagraph;

    @Value("${chpl.email.valediction}")
    private String chplEmailValediction;

    @Value("${footer.publicUrl}")
    private String publicFeedbackUrl;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Deprecated Api Usage Email job. *********");

        try {
            List<DeprecatedApiUsage> allDeprecatedApiUsage = deprecatedApiUsageDao.getAllDeprecatedApiUsage();
            LOGGER.info(allDeprecatedApiUsage.size() + " records of deprecated API usage were retrieved.");
            List<DeprecatedResponseFieldApiUsage> allDeprecatedResponseFieldUsage = deprecatedResponseFieldApiUsageDao.getAllUsage();
            LOGGER.info(allDeprecatedResponseFieldUsage.size() + " records of APIs with deprecated response field usage were retrieved.");

            Set<ApiKey> apiKeys = getDistinctApiKeys(allDeprecatedApiUsage, allDeprecatedResponseFieldUsage);
            LOGGER.info(apiKeys.size() + " API Keys accessed deprecated APIs.");

            apiKeys.stream()
                .forEach(apiKey -> sendUsageEmailAndDeleteUsageRecords(
                        apiKey,
                        allDeprecatedApiUsage.stream()
                            .filter(usage -> usage.getApiKey().equals(apiKey))
                            .collect(Collectors.toList()),
                        allDeprecatedResponseFieldUsage.stream()
                            .filter(usage -> usage.getApiKey().equals(apiKey))
                            .collect(Collectors.toList())));

        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the Deprecated Api Usage Email job. *********");
    }

    private Set<ApiKey> getDistinctApiKeys(List<DeprecatedApiUsage> allDeprecatedApiUsage,
            List<DeprecatedResponseFieldApiUsage> allDeprecatedResponseFieldUsage) {
        Set<ApiKey> distinctApiKeys = new LinkedHashSet<ApiKey>();
        distinctApiKeys.addAll(
                allDeprecatedApiUsage.stream()
                    .map(usage -> usage.getApiKey())
                    .distinct()
                    .collect(Collectors.toList()));
        distinctApiKeys.addAll(
                allDeprecatedResponseFieldUsage.stream()
                    .map(usage -> usage.getApiKey())
                    .distinct()
                    .collect(Collectors.toList()));
        return distinctApiKeys;
    }

    private void sendUsageEmailAndDeleteUsageRecords(ApiKey apiKey, List<DeprecatedApiUsage> deprecatedApiUsage,
            List<DeprecatedResponseFieldApiUsage> deprecatedResponseFieldUsage) {
        LOGGER.info("API Key " + apiKey.getId() + " for " + apiKey.getEmail() + " has used " + deprecatedApiUsage.size() + " deprecated APIs.");
        LOGGER.info("API Key " + apiKey.getId() + " for " + apiKey.getEmail() + " has used " + deprecatedResponseFieldUsage.size() + " APIs with deprecated response fields.");

        EmailBuilder emailBuilder = new EmailBuilder(env);
        try {
            emailBuilder.recipient(apiKey.getEmail())
                .subject(deprecatedApiUsageEmailSubject)
                .htmlMessage(createHtmlMessage(apiKey, deprecatedApiUsage, deprecatedResponseFieldUsage))
                .sendEmail();
            LOGGER.info("Sent email to " + apiKey.getEmail() + ".");
            deprecatedApiUsage.stream().forEach(item -> deleteDeprecatedApiUsage(item));
            LOGGER.info("Deleted " + deprecatedApiUsage.size() + " deprecated API usage records for " + apiKey.getEmail());
            deprecatedResponseFieldUsage.stream().forEach(item -> deleteDeprecatedResponseFieldUsage(item));
            LOGGER.info("Deleted " + deprecatedResponseFieldUsage.size() + " deprecated response field usage records for " + apiKey.getEmail());
        } catch (Exception ex) {
            LOGGER.error("Unable to send email to " + apiKey.getEmail() + ". "
                    + "User may not have been notified and database records will not be deleted.", ex);
        }
    }

    private String createHtmlMessage(ApiKey apiKey, List<DeprecatedApiUsage> deprecatedApiUsage,
            List<DeprecatedResponseFieldApiUsage> deprecatedResponseFieldUsage) throws IOException {
        List<String> apiUsageHeading  = Stream.of("HTTP Method", "API Endpoint", "Usage Count", "Last Accessed", "Message").collect(Collectors.toList());
        List<List<String>> apiUsageData = new ArrayList<List<String>>();
        deprecatedApiUsage.stream().forEach(api -> apiUsageData.add(createEndpointUsageData(api)));

        String deprecatedFieldHtml = buildDeprecatedResponseFieldHtml(deprecatedResponseFieldUsage);

        String htmlMessage = chplHtmlEmailBuilder.initialize()
                .heading(deprecatedApiUsageEmailHeading)
                .paragraph(
                        String.format(chplEmailGreeting, apiKey.getName()),
                        String.format(deprecatedApiUsageEmailBody, apiKey.getKey()))
                .paragraph(deprecatedApiParagraph, null, "h3")
                .table(apiUsageHeading, apiUsageData)
                .paragraph(deprecatedResponseFieldParagraph, null, "h3")
                .customHtml(deprecatedFieldHtml)
                .paragraph("", String.format(chplEmailValediction, publicFeedbackUrl))
                .footer(true)
                .build();
        LOGGER.debug("HTML Email being sent to " + apiKey.getEmail() + ": \n" + htmlMessage);
        return htmlMessage;
    }

    private List<String> createEndpointUsageData(DeprecatedApiUsage deprecatedApiUsage) {
        return Stream.of(deprecatedApiUsage.getApi().getApiOperation().getHttpMethod().name(),
                deprecatedApiUsage.getApi().getApiOperation().getEndpoint(),
                deprecatedApiUsage.getCallCount().toString(),
                getEasternTimeDisplay(deprecatedApiUsage.getLastAccessedDate()),
                deprecatedApiUsage.getApi().getChangeDescription()).collect(Collectors.toList());
    }

    //break out deprecated response field usage by endpoint
    //with a table of response fields under a paragraph about each endpoint
    private String buildDeprecatedResponseFieldHtml(List<DeprecatedResponseFieldApiUsage> deprecatedResponseFieldUsage) {
        StringBuffer htmlBuf = new StringBuffer();
        if (CollectionUtils.isEmpty(deprecatedResponseFieldUsage)) {
            htmlBuf.append(chplHtmlEmailBuilder.getParagraphHtml(null, "No Applicable Data", null));
        } else {
            for (DeprecatedResponseFieldApiUsage usage : deprecatedResponseFieldUsage) {
                String currUsageEndpointInfo = usage.getApi().getApiOperation().getHttpMethod()
                        + " of " + usage.getApi().getApiOperation().getEndpoint()
                        + "<br/>API Call Count: " + usage.getCallCount()
                        + "<br/>Last Accessed Date: " + getEasternTimeDisplay(usage.getLastAccessedDate());
                htmlBuf.append(chplHtmlEmailBuilder.getParagraphHtml(null, currUsageEndpointInfo, null));
                List<String> responseFieldUsageHeading  = Stream.of("Response Field", "Message", "Estimated Removal Date").collect(Collectors.toList());
                List<List<String>> responseFieldUsageData = createResponseFieldUsageData(usage);
                htmlBuf.append(chplHtmlEmailBuilder.getTableHtml(responseFieldUsageHeading, responseFieldUsageData, null));
            }
        }
        return htmlBuf.toString();
    }

    private List<List<String>> createResponseFieldUsageData(DeprecatedResponseFieldApiUsage usage) {
        List<List<String>> data = new ArrayList<List<String>>();
        usage.getApi().getResponseFields().sort(new Comparator<DeprecatedResponseField>() {
            @Override
            public int compare(DeprecatedResponseField o1, DeprecatedResponseField o2) {
                return o1.getResponseField().compareTo(o2.getResponseField());
            }
        });

        usage.getApi().getResponseFields().stream()
            .forEach(responseField -> data.add(Stream.of(
                    responseField.getResponseField(),
                    responseField.getChangeDescription(),
                    DateUtil.format(responseField.getRemovalDate()))
                    .collect(Collectors.toList())));
        return data;
    }

    private String getEasternTimeDisplay(Date date) {
        return DateUtil.formatInEasternTime(date, "MMM d, yyyy, hh:mm");
    }

    private void deleteDeprecatedApiUsage(DeprecatedApiUsage usage) {
        try {
            deprecatedApiUsageDao.delete(usage.getId());
            LOGGER.info("Deleted deprecated API usage with ID " + usage.getId());
        } catch (Exception ex) {
            LOGGER.error("Error deleting deprecated API usage with ID " + usage.getId(), ex);
        }
    }

    private void deleteDeprecatedResponseFieldUsage(DeprecatedResponseFieldApiUsage usage) {
        try {
            deprecatedResponseFieldApiUsageDao.delete(usage.getId());
            LOGGER.info("Deleted deprecated response field usage with ID " + usage.getId());
        } catch (Exception ex) {
            LOGGER.error("Error deleting deprecated response field usage with ID " + usage.getId(), ex);
        }
    }
}
