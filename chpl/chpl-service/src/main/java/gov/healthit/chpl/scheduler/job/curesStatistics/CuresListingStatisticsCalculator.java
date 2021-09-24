package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.CuresListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.statistics.CuresListingStatistic;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.service.CuresUpdateService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CuresListingStatisticsCalculator {
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertifiedProductDAO certifiedProductDAO;
    private CuresListingStatisticsDAO curesListingStatisticsDAO;

    private List<CertificationCriterion> curesCriteria;

    @Autowired
    public CuresListingStatisticsCalculator(CertifiedProductDetailsManager certifiedProductDetailsManager, CertifiedProductDAO certifiedProductDAO,
            CuresUpdateService curesUpdateService, CertificationCriterionService certificationCriterionService, CuresListingStatisticsDAO curesListingStatisticsDAO) {
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certifiedProductDAO = certifiedProductDAO;
        this.curesListingStatisticsDAO = curesListingStatisticsDAO;

        populateCuresCriteria(certificationCriterionService);
    }

    public List<CuresListingStatistic> calculate(LocalDate statisticDate) {

        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            List<CertifiedProductSearchDetails> listings = pool.submit(() -> get2015ListingDetails()).get();

            CuresListingStatistic stat = new CuresListingStatistic();
            stat.setCuresListingWithoutCuresCriteriaCount(calculateCuresUpdateListingsWithoutCuresCriteriaCount(listings));
            stat.setCuresListingWithCuresCriteriaCount(calculateCuresUpdatedListingsWithCuresCriteriaCount(listings));
            stat.setNonCuresListingCount(calculateNonCuresListingCount(listings));
            stat.setStatisticDate(statisticDate);

            return Arrays.asList(stat);
        } catch (Exception e) {
            LOGGER.catching(e);
            return new ArrayList<CuresListingStatistic>();
        }

    }

    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CuresListingStatistic> statisticsForDate = curesListingStatisticsDAO.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CuresListingStatistic> statisticsForDate = curesListingStatisticsDAO.getStatisticsForDate(statisticDate);
        for (CuresListingStatistic statistic : statisticsForDate) {
            try {
                curesListingStatisticsDAO.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }

    public void save(List<CuresListingStatistic> statistics) {
        statistics.stream()
            .forEach(stat -> stat.setLastModifiedUser(User.SYSTEM_USER_ID));

        curesListingStatisticsDAO.create(statistics);
    }

    private Long calculateCuresUpdateListingsWithoutCuresCriteriaCount(List<CertifiedProductSearchDetails> listings) {
        return listings.stream()
                .filter(listing -> listing.getCuresUpdate()
                        && !doesListingAttestToAnyCuresCriteria(listing))
                .collect(Collectors.counting());
    }

    private Long calculateCuresUpdatedListingsWithCuresCriteriaCount(List<CertifiedProductSearchDetails> listings) {
        return listings.stream()
                .filter(listing -> listing.getCuresUpdate()
                        && doesListingAttestToAnyCuresCriteria(listing))
                .collect(Collectors.counting());
    }

    private Long calculateNonCuresListingCount(List<CertifiedProductSearchDetails> listings) {
        return listings.stream()
                .filter(listing -> !listing.getCuresUpdate())
                .collect(Collectors.counting());
    }

    List<CertifiedProductSearchDetails> get2015ListingDetails() {
        return getAll2015CertifiedProducts().parallelStream()
                .map(detail -> getDetails(detail.getId()))
                .collect(Collectors.toList());
    }

    private Boolean doesListingAttestToAnyCuresCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess())
                .filter(cr -> isCuresCriterion(cr.getCriterion()))
                .findFirst()
                .isPresent();
    }

    private boolean isCuresCriterion(CertificationCriterion criterion) {
        return curesCriteria.stream()
                .filter(cc -> cc.getId().equals(criterion.getId()))
                .findFirst()
                .isPresent();
    }

    private CertifiedProductSearchDetails getDetails(Long id) {
        try {
            Long start = (new Date()).getTime();
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(id);
            Long end = (new Date()).getTime();
            LOGGER.info("Retrieved Listing Details for " + id + " in " + (end - start) + "ms");
            return listing;
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private List<CertifiedProductDetailsDTO> getAll2015CertifiedProducts() {
        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");
        return listings.stream()
                .collect(Collectors.toList());
    }

    private void populateCuresCriteria(CertificationCriterionService certificationCriterionService) {
        curesCriteria = certificationCriterionService.getOriginalToCuresCriteriaMap().entrySet().stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
        //add D12 and D13
        curesCriteria.add(certificationCriterionService.get(Criteria2015.D_12));
        curesCriteria.add(certificationCriterionService.get(Criteria2015.D_13));
    }


}
