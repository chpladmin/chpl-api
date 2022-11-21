package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MissingAttestationChangeRequestEmailJob implements Job  {

    @Autowired
    private MissingAttestationChangeRequestDeveloperCollector developerCollector;

    @Autowired
    private MissingAttestationChangeRequestDeveloperEmailGenerator emailGenerator;

    @Autowired ChplEmailFactory emailFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Missing Attestatation Change Request Email job. *********");
        List<DeveloperEmail> developerEmails = developerCollector.getDevelopers().stream()
                .map(developer -> emailGenerator.getDeveloperEmail(developer))
                //.peek(email -> LOGGER.info(email.getMessage()))
                .toList();

        sendEmails(developerEmails);
        LOGGER.info("********* Completed Developer Missing Attestatation Change Request Email job. *********");
    }

    private void sendEmails(List<DeveloperEmail> developerEmails) {
        developerEmails.forEach(email -> {
            try {
                emailFactory.emailBuilder()
                    .recipients(email.getRecipients())
                    .subject(email.getSubject())
                    .htmlMessage(email.getMessage())
                    .sendEmail();
            } catch (Exception e) {
                LOGGER.error("Error sending emails to developer : {}", email.getDeveloper().getName());
                LOGGER.error(e);
            }
        });
    }
}
