package gov.healthit.chpl.scheduler.job;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "realWorldTestingReportEmailJobLogger")
public class RealWorldTestingReportEmailJob implements Job {

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Report Email job. *********");

        try {
           List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
           listings = listings.stream()
                   .filter(listing -> isListingRwtEligible(listing.getRwtEligibilityYear()))
                   .collect(Collectors.toList());

        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Report Email job. *********");
        }
    }

    private boolean isListingRwtEligible(Integer rwtEligYear) {
        return rwtEligYear != null;
    }
}
