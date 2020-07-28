package gov.healthit.chpl.scheduler.job;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RealWorldTestingEligibilityJob extends QuartzJob {
    //private static final Logger LOGGER = LogManager.getLogger("realWorldTestingEligibilityJobLogger");
    private static final String ACTIVE = "Active";

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private CertificationStatusEventDAO certificationStatusEventDAO;

    @Autowired
    private CertificationCriterionService certificationCriterionService;

    @Value("${realWorldTesting.criteria}")
    private String[] eligibleCriteriaKeys;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("********* Starting the Real World Testing Eligibility job. *********");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Calendar asOfDate = Calendar.getInstance();
        asOfDate.set(2020, 0, 1, 0, 0, 0);

        List<CertificationCriterion> eligibleCriteria = getRwtEligibleCriteria();

        //This will get us all of the listings that we still need to check criteria eligibility (reduce calls for details)
        List<CertifiedProductDetailsDTO> listings = getAllListingsWith2015Edition().stream()
                .filter(listing -> !doesListingHaveExistingRwtEligibility(listing)
                        && isListingStatusActiveAsOfDate(listing.getId(), asOfDate.getTime()))
                .collect(Collectors.toList());

        getCertifiedProductDetails(listings).stream()
        .filter(detail -> doesListingAttestToEligibleCriteria(detail, eligibleCriteria))
        .forEach(detail -> updateRwtEligiblityYear(detail));

    }

    private void updateRwtEligiblityYear(CertifiedProductSearchDetails detail) {
        try {
            CertifiedProductDTO dto = certifiedProductDAO.getById(detail.getId());
            dto.setRwtEligiblityYear(getEligibilityYear());
            certifiedProductDAO.update(dto);
            LOGGER.info("Listing: " + detail.getId() + " - Added eligibility");
        } catch (EntityRetrievalException e) {
            LOGGER.error("Listing: " + detail.getId() + " - Error setting eligibility", e);
        }
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetails(List<CertifiedProductDetailsDTO> listings) {
        return listings.parallelStream()
                .map(cp -> getCertifiedProductSearchDetails(cp.getId()))
                .peek(cp -> LOGGER.info("Listing: " + cp.getId() + " - Retreived details"))
                .collect(Collectors.toList());
    }

    private boolean doesListingAttestToEligibleCriteria(
            CertifiedProductSearchDetails listing, List<CertificationCriterion> eligibleCriteria) {
        boolean doesExist = listing.getCertificationResults().stream()
                .filter(result -> result.isSuccess()
                        && eligibleCriteria.stream()
                        .filter(crit -> crit.getId().equals(result.getCriterion().getId()))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();

        if (doesExist) {
            return true;
        } else {
            LOGGER.info("Listing: " + listing.getId() + " - Does not attest to any eligible criteria");
            return false;
        }
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long listingId) {
        try {
            return certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
        } catch (Exception e) {
            LOGGER.error("Could not retrieve the details for listing: " + listingId);
            throw new RuntimeException(e);
        }
    }

    private List<CertificationCriterion> getRwtEligibleCriteria() {
        return Arrays.asList(eligibleCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }
    private List<CertifiedProductDetailsDTO> getAllListingsWith2015Edition() {
        LOGGER.info("Getting all listings.");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completing getting all listings. Found " + listings.size() + " listings.");
        return listings;
    }

    private boolean doesListingHaveExistingRwtEligibility(CertifiedProductDetailsDTO listing) {
        if (Objects.nonNull(listing.getRwtEligibilityYear())) {
            LOGGER.info("Listing: " + listing.getId() + " - Already RWT eligible");
            return true;
        } else {
            return false;
        }
    }

    private boolean isListingStatusActiveAsOfDate(Long listingId, Date asOfDate) {
        CertificationStatusEventDTO event = getListingStatusAsOf(listingId, asOfDate);
        if (Objects.nonNull(event)
                && event.getStatus().getStatus().equals(RealWorldTestingEligibilityJob.ACTIVE)) {
            return true;
        } else {
            LOGGER.info("Listing: " + listingId + " - Not Active");
            return false;
        }
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

    private Integer getEligibilityYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + 1;
    }
}
