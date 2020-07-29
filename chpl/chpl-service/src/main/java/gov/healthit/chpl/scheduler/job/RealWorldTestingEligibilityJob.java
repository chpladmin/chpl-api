package gov.healthit.chpl.scheduler.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.NoArgsConstructor;

public class RealWorldTestingEligibilityJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("realWorldTestingEligibilityJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    @Qualifier("rwtEligibilityYearDAO")
    private RwtEligibilityYearDAO rwtEligibilityYearDAO;

    @Autowired
    private CertificationStatusEventDAO certificationStatusEventDAO;

    @Autowired
    private CertificationCriterionService certificationCriterionService;

    @Value("${realWorldTestingCriteriaKeys}")
    private String[] eligibleCriteriaKeys;

    @Value("${rwtPlanStartDayOfYear}")
    private String rwtPlanStartDayOfYear;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("********* Starting the Real World Testing Eligibility job. *********");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Date asOfDate = getEligibilityAsOfDate();
        List<CertificationCriterion> eligibleCriteria = getRwtEligibleCriteria();

        //This will get us all of the listings that we still need to check criteria eligibility (reduce calls for details)
        List<CertifiedProductDetailsDTO> listings = getAllListingsWith2015Edition().stream()
                .filter(listing -> !doesListingHaveExistingRwtEligibility(listing))
                .collect(Collectors.toList());

        getCertifiedProductDetails(listings).stream()
        .filter(detail -> isListingStatusActiveAsOfDate(detail, asOfDate)
                && doesListingAttestToEligibleCriteria(detail, eligibleCriteria))
        .forEach(detail -> updateRwtEligiblityYear(detail));
        LOGGER.info("********* Completed the Real World Testing Eligibility job. *********");
    }

    private void updateRwtEligiblityYear(CertifiedProductSearchDetails detail) {
        try {
            rwtEligibilityYearDAO.updateRwtEligibilityYear(detail.getId(), getEligibilityYear());
            LOGGER.info("Listing: " + detail.getId() + " - Added eligibility");
        } catch (EntityRetrievalException e) {
            LOGGER.error("Listing: " + detail.getId() + " - Error setting eligibility", e);
        }
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetails(List<CertifiedProductDetailsDTO> listings) {
        //Using a `parallelStream` where we supply a ThreadPool to control the number of threads being used
        ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);

        try {
            return customThreadPool.submit(() ->
            listings.parallelStream()
            .map(cp -> getCertifiedProductSearchDetails(cp.getId()))
            .peek(cp -> LOGGER.info("Listing: " + cp.getId() + " - Retreived details"))
            .collect(Collectors.toList())).get();
        } catch (Exception e) {
            LOGGER.error("Could not retrieve details for listings.", e);
            throw new RuntimeException("Could not retrieve details for listings.", e);
        }

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
            CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
            return detail;
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
        LOGGER.info("Getting all 2015 listings.");
        List<CertifiedProductDetailsDTO> listings = rwtEligibilityYearDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completing getting all 2015 listings. Found " + listings.size() + " listings.");
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

    private boolean isListingStatusActiveAsOfDate(CertifiedProductSearchDetails listing, Date asOfDate) {
        CertificationStatusEvent event = listing.getStatusOnDate(asOfDate);
        if (Objects.nonNull(event)
                && event.getStatus().getName().equals(CertificationStatusType.Active.getName())) {
            return true;
        } else {
            LOGGER.info("Listing: " + listing.getId() + " - Not Active");
            return false;
        }
    }

    private Integer getEligibilityYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + 1;
    }

    private Date getEligibilityAsOfDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        try {
            calendar.setTime(sdf.parse(rwtPlanStartDayOfYear + "/" + calendar.get(Calendar.YEAR)));
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTime();
        } catch (ParseException e) {
            LOGGER.error("Could not calculate 'asOfDate'.", e);
            throw new RuntimeException("Could not calculate 'asOfDate'.", e);
        }
    }

    @Component("rwtEligibilityYearDAO")
    @NoArgsConstructor
    private static class RwtEligibilityYearDAO extends CertifiedProductDAO {
        @Transactional
        public void updateRwtEligibilityYear(Long listingId, Integer year) throws EntityRetrievalException {
            CertifiedProductEntity entity = getEntityById(listingId);
            entity.setRwtEligibilityYear(year);
            try {
                update(entity);
            } catch (Exception ex) {
                LOGGER.error("Could not update rwtEligibilityYear for listing: " + listingId, ex);
                throw new EntityRetrievalException("Could not update rwtEligibilityYear for listing: " + listingId, ex);
            }
        }
    }

}
