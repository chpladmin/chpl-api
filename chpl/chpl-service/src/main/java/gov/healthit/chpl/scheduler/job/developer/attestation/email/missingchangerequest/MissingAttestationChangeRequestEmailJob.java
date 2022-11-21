package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MissingAttestationChangeRequestEmailJob implements Job  {

    @Autowired
    private MissingAttestationChangeRequestDeveloperCollector developerCollector;

    @Autowired
    private MissingAttestationChangeRequestDeveloperEmailGenerator emailGenerator;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Missing Attestatation Change Request Email job. *********");
        List<DeveloperEmail> developerEmails = developerCollector.getDevelopers().stream()
                .map(developer -> emailGenerator.getDeveloperEmail(developer))
                .peek(email -> LOGGER.info(email.getMessage()))
                .toList();

        LOGGER.info("********* Completed Developer Missing Attestatation Change Request Email job. *********");
    }
}
