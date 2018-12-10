package gov.healthit.chpl.scheduler.job;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.EmailBuilder;
import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ApiKeyManager;

public class ApiKeyWarningEmailJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("apiKeyWarningEmailJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the API Key Warning Email job. *********");
        LOGGER.info("Looking for API keys that have not been used in " + getNumberOfDaysForWarning() + " days.");

        List<ApiKeyDTO> apiKeyDTOs = apiKeyDAO.findAllNotUsedInXDays(getNumberOfDaysForWarning());

        LOGGER.info("Found " + apiKeyDTOs.size() + " API keys to send warnings for.");

        for (ApiKeyDTO dto : apiKeyDTOs) {
            try {
                updateDeleteWarningSentDate(dto);
                sendEmail(dto);
            } catch (EntityRetrievalException e) {
                LOGGER.error("Error updating api_key.delete_warning_sent_date for id: " + dto.getId(), e);
            } catch (MessagingException e) {
                LOGGER.error("Error sending email to: " + dto.getEmail(), e);
            }
        }

        LOGGER.info("********* Completed the API Key Warning Email job. *********");
    }

    private void updateDeleteWarningSentDate(ApiKeyDTO dto) throws EntityRetrievalException {
        dto.setDeleteWarningSentDate(new Date());
        apiKeyManager.updateApiKey(dto);
    }

    private void sendEmail(ApiKeyDTO dto) throws AddressException, MessagingException {
        List<String> recipients = new ArrayList<String>();
        recipients.add(dto.getEmail());

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(recipients)
                        .subject(getSubject())
                        .htmlMessage(getHtmlMessage(dto))
                        .sendEmail();
        LOGGER.info("Email sent to: " + dto.getEmail());
    }

    private String getHtmlMessage(ApiKeyDTO dto) {
        String message = String.format(
                env.getProperty("job.apiKeyWarningEmailJob.config.message"),
                dto.getNameOrganization(),
                getTotalDaysUnusedBeforeDelete().toString(),
                dto.getApiKey(),
                getDateFormatter().format(dto.getLastUsedDate()),
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
