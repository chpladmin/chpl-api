package gov.healthit.chpl.scheduler.job.deprecatedApiUsage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "deprecatedApiUsageEmailJobLogger")
public class DeprecatedApiUsageEmailJob implements Job {
    @Autowired
    private DeprecatedApiUsageDao deprecatedApiUsageDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

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
            List<DeprecatedApiUsage> allDeprecatedApiUsage = deprecatedApiUsageDao.getUnnotifiedUsage();
            LOGGER.info(allDeprecatedApiUsage.size() + " records of deprecated API usage were retrieved.");

            Set<ApiKey> apiKeys = getDistinctApiKeys(allDeprecatedApiUsage);
            LOGGER.info(apiKeys.size() + " distinct API Keys were found.");

            apiKeys.stream()
                .forEach(apiKey -> sendUsageEmailAndDeleteUsageRecords(apiKey,
                        allDeprecatedApiUsage.stream()
                            .filter(usage -> usage.getApiKey().equals(apiKey))
                            .collect(Collectors.toList())));

        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the Deprecated Api Usage Email job. *********");
    }

    private Set<ApiKey> getDistinctApiKeys(List<DeprecatedApiUsage> allDeprecatedApiUsage) {
        Set<ApiKey> distinctApiKeys = new LinkedHashSet<ApiKey>();
        distinctApiKeys.addAll(
                allDeprecatedApiUsage.stream()
                    .map(usage -> usage.getApiKey())
                    .distinct()
                    .collect(Collectors.toList()));
        return distinctApiKeys;
    }

    private void sendUsageEmailAndDeleteUsageRecords(ApiKey apiKey, List<DeprecatedApiUsage> allUsage) {
        long deprecatedApisUsedCount = getDeprecatedApiUsage(allUsage).size();
        long apisWithDeprecatedResponseFieldsUsedCount = getApisUsedWithDeprecatedResponseFields(allUsage)
                .keySet()
                .size();

        LOGGER.info("API Key " + apiKey.getId() + " for " + apiKey.getEmail() + " has used " + deprecatedApisUsedCount + " deprecated APIs.");
        LOGGER.info("API Key " + apiKey.getId() + " for " + apiKey.getEmail() + " has used " + apisWithDeprecatedResponseFieldsUsedCount + " APIs with deprecated response fields.");

        EmailBuilder emailBuilder = chplEmailFactory.emailBuilder();
        try {
            emailBuilder.recipient(apiKey.getEmail())
                .subject(deprecatedApiUsageEmailSubject)
                .htmlMessage(createHtmlMessage(apiKey, allUsage))
                .sendEmail();
            LOGGER.info("Sent email to " + apiKey.getEmail() + ".");
            allUsage.stream().forEach(item -> markAsUserNotified(item));
            LOGGER.info("Marked " + allUsage.size() + " deprecated API usage records as user-notified for " + apiKey.getEmail());
        } catch (Exception ex) {
            LOGGER.error("Unable to send email to " + apiKey.getEmail() + ". "
                    + "User may not have been notified and database records will not be deleted.", ex);
        }
    }

    private List<DeprecatedApiUsage> getDeprecatedApiUsage(List<DeprecatedApiUsage> allUsage) {
        return allUsage.stream()
                .filter(usage -> usage.getResponseField() == null)
                .collect(Collectors.toList());
    }

    private Map<String, List<DeprecatedApiUsage>> getApisUsedWithDeprecatedResponseFields(List<DeprecatedApiUsage> allUsage) {
        return allUsage.stream()
                .filter(usage -> usage.getResponseField() != null)
                .collect(Collectors.groupingBy(DeprecatedApiUsage::getEndpoint));
    }

    private String createHtmlMessage(ApiKey apiKey, List<DeprecatedApiUsage> allUsage) throws IOException {
        List<String> apiUsageHeading  = Stream.of("HTTP Method", "API Endpoint", "Usage Count", "Last Accessed", "Message", "Estimated Removal Date").collect(Collectors.toList());
        List<List<String>> apiUsageData = new ArrayList<List<String>>();
        getDeprecatedApiUsage(allUsage).stream().forEach(api -> apiUsageData.add(createEndpointUsageData(api)));

        Map<String, List<DeprecatedApiUsage>> deprecatedResponseFieldUsage = getApisUsedWithDeprecatedResponseFields(allUsage);
        String deprecatedFieldHtml = buildDeprecatedResponseFieldsHtml(deprecatedResponseFieldUsage);

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
        return Stream.of(deprecatedApiUsage.getHttpMethod(),
                deprecatedApiUsage.getApiOperation(),
                deprecatedApiUsage.getCallCount().toString(),
                getEasternTimeDisplay(deprecatedApiUsage.getLastAccessedDate()),
                deprecatedApiUsage.getMessage(),
                DateUtil.format(deprecatedApiUsage.getRemovalDate())).collect(Collectors.toList());
    }

    //break out deprecated response field usage by endpoint
    //with a table of response fields under a paragraph about each endpoint
    private String buildDeprecatedResponseFieldsHtml(Map<String, List<DeprecatedApiUsage>> deprecatedResponseFieldUsage) {
        StringBuffer htmlBuf = new StringBuffer();
        if (CollectionUtils.isEmpty(deprecatedResponseFieldUsage.keySet())) {
            htmlBuf.append(chplHtmlEmailBuilder.getParagraphHtml(null, "No Applicable Data", null));
        } else {
            deprecatedResponseFieldUsage.keySet().stream()
                .forEach(endpoint -> htmlBuf.append(buildDeprecatedResponseFieldHtml(deprecatedResponseFieldUsage.get(endpoint))));
        }
        return htmlBuf.toString();
    }

    private String buildDeprecatedResponseFieldHtml(List<DeprecatedApiUsage> usage) {
        String currUsageEndpointInfo = usage.get(0).getHttpMethod() + " of " + usage.get(0).getApiOperation()
            + "<br/>API Call Count: " + usage.get(0).getCallCount()
            + "<br/>Last Accessed Date: " + getEasternTimeDisplay(usage.get(0).getLastAccessedDate());
        String html = chplHtmlEmailBuilder.getParagraphHtml(null, currUsageEndpointInfo, null);
        List<String> responseFieldUsageHeading  = Stream.of("Response Field", "Message", "Estimated Removal Date").collect(Collectors.toList());
        List<List<String>> responseFieldUsageData = createResponseFieldUsageData(usage);
        html += chplHtmlEmailBuilder.getTableHtml(responseFieldUsageHeading, responseFieldUsageData, null);
        return html;
    }

    private List<List<String>> createResponseFieldUsageData(List<DeprecatedApiUsage> endpointUsage) {
        List<List<String>> data = new ArrayList<List<String>>();
        endpointUsage.sort(new Comparator<DeprecatedApiUsage>() {
            @Override
            public int compare(DeprecatedApiUsage o1, DeprecatedApiUsage o2) {
                return o1.getResponseField().compareTo(o2.getResponseField());
            }
        });

        endpointUsage.stream()
            .forEach(usage -> data.add(Stream.of(
                    usage.getResponseField(),
                    usage.getMessage(),
                    DateUtil.format(usage.getRemovalDate()))
                    .collect(Collectors.toList())));
        return data;
    }

    private String getEasternTimeDisplay(Date date) {
        return DateUtil.formatInEasternTime(date, "MMM d, yyyy, hh:mm");
    }

    private void markAsUserNotified(DeprecatedApiUsage usage) {
        try {
            deprecatedApiUsageDao.markAsUserNotified(usage.getId());
            LOGGER.info("Deleted deprecated API usage with ID " + usage.getId());
        } catch (Exception ex) {
            LOGGER.error("Error deleting deprecated API usage with ID " + usage.getId(), ex);
        }
    }
}
