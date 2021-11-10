package gov.healthit.chpl.scheduler.job;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ApiKeyDeleteJob implements Job {
private static final Logger LOGGER = LogManager.getLogger("apiKeyDeleteJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the API Key Deletion job. *********");
        LOGGER.info("Looking for API keys where the warning email was sent " + getNumberOfDaysUntilDelete() + " days ago.");

        List<ApiKey> apiKeys = apiKeyDAO.findAllToBeRevoked(getNumberOfDaysUntilDelete());

        LOGGER.info("Found " + apiKeys.size() + " API keys to delete.");

        for (ApiKey apiKey : apiKeys) {

            try {
                delete(apiKey);
                sendEmail(apiKey);
            } catch (EntityRetrievalException | EntityCreationException | JsonProcessingException e) {
                LOGGER.error("Error updating api_key.deleted for id: " + apiKey.getId(), e);
            } catch (AddressException | EmailNotSentException e) {
                LOGGER.error("Error sending email to: " + apiKey.getEmail(), e);
            }
        }

        LOGGER.info("********* Completed the API Key Deletion job. *********");
    }

    private void delete(ApiKey apiKey) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        apiKeyManager.deleteKey(apiKey.getId());
    }

    private void sendEmail(ApiKey apiKey) throws AddressException, EmailNotSentException {
        List<String> recipients = new ArrayList<String>();
        recipients.add(apiKey.getEmail());

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(recipients)
                        .subject(getSubject())
                        .htmlMessage(getHtmlMessage(apiKey))
                        .sendEmail();
        LOGGER.info("Email sent to: " + apiKey.getEmail());
    }

    private String getHtmlMessage(ApiKey apiKey) {
        String message = String.format(
                env.getProperty("job.apiKeyDeleteJob.config.message"),
                apiKey.getName(),
                apiKey.getKey(),
                getDateFormatter().format(apiKey.getLastUsedDate()),
                env.getProperty("chplUrlBegin"),
                env.getProperty("chplUrlBegin"));

        return message;
    }

    private String getSubject() {
        return env.getProperty("job.apiKeyDeleteJob.config.subject");
    }

    private DateFormat getDateFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.LONG,
                 Locale.US);
    }

    private Integer getNumberOfDaysUntilDelete() {
        return Integer.valueOf(env.getProperty("job.apiKeyWarningEmailJob.config.daysUntilDelete"));
    }
}
