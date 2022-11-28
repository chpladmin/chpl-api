package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.StatusReportEmail;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MissingAttestationChangeRequestEmailJob implements Job  {

    @Autowired
    private MissingAttestationChangeRequestDeveloperCollector missingAttestationChangeRequestDeveloperCollector;

    @Autowired
    private MissingAttestationChangeRequestDeveloperEmailGenerator emailGenerator;

    @Autowired
    private MissingAttestationChangeRequestDeveloperStatusReportEmailGenerator emailStatusReportGenerator;

    @Autowired
    private ChplEmailFactory emailFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Missing Attestatation Change Request Email job. *********");
        List<DeveloperEmail> developerEmails = missingAttestationChangeRequestDeveloperCollector.getDevelopers().stream()
                .map(developer -> emailGenerator.getDeveloperEmail(developer))
                .toList();

        sendEmails(developerEmails);

        sendStatusReportEmail(developerEmails);

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

    private void sendStatusReportEmail(List<DeveloperEmail> developerEmails) {
        StatusReportEmail statusReportEmail = emailStatusReportGenerator.getStatusReportEmail(developerEmails);

        try {
            emailFactory.emailBuilder()
                .recipients(statusReportEmail.getRecipients())
                .subject(statusReportEmail.getSubject())
                .htmlMessage(statusReportEmail.getMessage())
                .sendEmail();
        } catch (Exception e) {
            LOGGER.error("Error sending status report emails to: {}", statusReportEmail.getRecipients().stream().collect(Collectors.joining("; ")));
            LOGGER.error(e);
        }
    }
}
