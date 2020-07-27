package gov.healthit.chpl.scheduler.job;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public class RealWorldTestingEligibilityJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("realWorldTestingEligibilityJobLogger");

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private CertificationStatusEventDAO certificationStatusEventDAO;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("********* Starting the Real World Testing Eligibility job. *********");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Calendar asOfDate = Calendar.getInstance();
        asOfDate.set(2020, 0, 1, 0, 0, 0);

        getAllListingsWith2015Edition().stream()
        .forEach(listing -> LOGGER.info(listing.getId() + " : " + getListingStatusAsOf(listing.getId(), asOfDate.getTime())));
    }

    private List<CertifiedProductDetailsDTO> getAllListingsWith2015Edition() {
        LOGGER.info("Getting all listings.");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completing getting all listings. Found " + listings.size() + " listings.");
        return listings;
    }

    private boolean doesListingHaveExistingRwtEligibility(CertifiedProductDetailsDTO listing) {
        return Objects.nonNull(listing.getRwtEligibilityYear());
    }

    private CertificationStatusEventDTO getListingStatusAsOf(Long listingId, Date asOfDate) {
        List<CertificationStatusEventDTO> events = certificationStatusEventDAO.findByCertifiedProductId(listingId);
        CertificationStatusEventDTO event = null;
        if (Objects.nonNull(events)) {
            events.sort(Comparator.comparing(e -> e.getEventDate()));
            event = events.stream()
                    .reduce(null, (foundEvent, e) -> asOfDate.after(e.getEventDate()) ? e : foundEvent);
        }
        return event;
    }
}
