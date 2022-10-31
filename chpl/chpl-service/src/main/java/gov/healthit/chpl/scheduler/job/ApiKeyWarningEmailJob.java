package gov.healthit.chpl.scheduler.job;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ApiKeyWarningEmailJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("apiKeyWarningEmailJobLogger");

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the API Key Warning Email job. *********");
        LOGGER.info("Looking for API keys that have not been used in " + getNumberOfDaysForWarning() + " days.");

        List<ApiKey> apiKeys = apiKeyDAO.findAllNotUsedInXDays(getNumberOfDaysForWarning());

        LOGGER.info("Found " + apiKeys.size() + " API keys to send warnings for.");
        try {
            // We need to manually create a transaction in this case because of how AOP works. When a method is
            // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
            // The object's proxy is not called when the method is called from within this class. The object's proxy
            // is called when the method is public and is called from a different object.
            // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {

                    for (ApiKey apiKey : apiKeys) {
                        try {
                            updateDeleteWarningSentDate(apiKey);
                            sendEmail(apiKey);
                        } catch (EntityRetrievalException e) {
                            LOGGER.error("Error updating api_key.delete_warning_sent_date for id: " + apiKey.getId(), e);
                        } catch (EmailNotSentException e) {
                            LOGGER.error("Error sending email to: " + apiKey.getEmail(), e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the API Key Warning Email job. *********");
    }

    private void updateDeleteWarningSentDate(ApiKey apiKey) throws EntityRetrievalException {
        apiKey.setDeleteWarningSentDate(new Date());
        apiKeyDAO.update(apiKey);
    }

    private void sendEmail(ApiKey apiKey) throws EmailNotSentException {
        List<String> recipients = new ArrayList<String>();
        recipients.add(apiKey.getEmail());

        chplEmailFactory.emailBuilder().recipients(recipients)
                        .subject(getSubject())
                        .htmlMessage(getHtmlMessage(apiKey))
                        .sendEmail();
        LOGGER.info("Email sent to: " + apiKey.getEmail());
    }

    private String getHtmlMessage(ApiKey apiKey) {
        String message = String.format(
                env.getProperty("job.apiKeyWarningEmailJob.config.message"),
                apiKey.getName(),
                getTotalDaysUnusedBeforeDelete().toString(),
                apiKey.getKey(),
                getDateFormatter().format(apiKey.getLastUsedDate()),
                getNumberOfDaysUntilDelete().toString());
        return message;
    }

    private String getSubject() {
        return env.getProperty("job.apiKeyWarningEmailJob.config.subject");
    }

    private DateFormat getDateFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.LONG,
                Locale.US);
    }

    private Integer getTotalDaysUnusedBeforeDelete() {
        return getNumberOfDaysForWarning() + getNumberOfDaysUntilDelete();
    }

    private Integer getNumberOfDaysForWarning() {
        return Integer.valueOf(env.getProperty("job.apiKeyWarningEmailJob.config.apiKeyLastUsedDaysAgo"));
    }

    private Integer getNumberOfDaysUntilDelete() {
        return Integer.valueOf(env.getProperty("job.apiKeyWarningEmailJob.config.daysUntilDelete"));
    }
}
