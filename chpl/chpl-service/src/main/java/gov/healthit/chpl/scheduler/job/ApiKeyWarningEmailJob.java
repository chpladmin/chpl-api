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

public class ApiKeyWarningEmailJob implements Job{
    private static final Logger LOGGER = LogManager.getLogger(ApiKeyWarningEmailJob.class);
    
    @Autowired
    private Environment env;
    
    @Autowired
    private ApiKeyDAO apiKeyDAO;
    
    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the API Key Warning Email job. *********");
        List<ApiKeyDTO> apiKeyDTOs = apiKeyDAO.findAllNotUsedInXDays(7);
        LOGGER.info("Found " + apiKeyDTOs.size() + " API keys to send warnings for.");
        
        for (ApiKeyDTO dto : apiKeyDTOs) {
            
            try {
                updateDeleteWarningSentDate(dto);
                sendEmail(dto);
            } catch (EntityRetrievalException e) {
                LOGGER.error("Error updating api_key.delete_warning_sent_date for id: " + dto.getId(), e);
            } catch (MessagingException e) {
                LOGGER.error("Error sending emai to: " + dto.getEmail(), e);
            } 
        }
        
        LOGGER.info("********* Completed the API Key Warning Email job. *********");
    }
    
    private void updateDeleteWarningSentDate(ApiKeyDTO dto) throws EntityRetrievalException {
        dto.setDeleteWarningSentDate(new Date());
        apiKeyDAO.update(dto);
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
        StringBuilder sb = new StringBuilder();
        sb.append("Email Address: ");
        sb.append(dto.getEmail());
        sb.append("<br/>");
        sb.append("Name/Organization: ");
        sb.append(dto.getNameOrganization());
        sb.append("<br/>");
        sb.append("Key Last Used Date: ");
        sb.append(getDateFormatter().format(dto.getLastUsedDate()));
        sb.append("<br/>");
        return sb.toString();
    }
    
    private String getSubject() {
        return "ONC-CHPL: Your API key will be deleted";
    }
    
    private DateFormat getDateFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG, 
                DateFormat.LONG, 
                Locale.US);
    }
}
