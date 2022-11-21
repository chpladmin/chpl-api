package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MissingAttestationChangeRequestEmailJob implements Job  {

    @Autowired
    private MissingAttestationChangeRequestDeveloperCollector developerCollector;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Missing Attestatation Change Request Email job. *********");
        developerCollector.getDevelopers().stream()
                .forEach(developer -> LOGGER.info("Found the following developer: {}", developer.getName()));

        LOGGER.info("********* Completed Developer Missing Attestatation Change Request Email job. *********");
    }
}
