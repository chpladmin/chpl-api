package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.CuresListingStatisticsByAcbDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.statistics.CuresListingStatisticByAcb;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
@Component
public class CuresListingByAcbStatisticsCalculator {
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertifiedProductDAO certifiedProductDAO;
    private CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private Integer threadCount;
    private List<CertificationCriterion> curesCriteria;
    private List<String> activeStatusNames;


    @Autowired
    public CuresListingByAcbStatisticsCalculator(CertifiedProductDetailsManager certifiedProductDetailsManager, CertifiedProductDAO certifiedProductDAO,
            CertificationCriterionService certificationCriterionService, CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO,
            CertificationBodyDAO certificationBodyDAO, @Value("${executorThreadCountForQuartzJobs}") Integer threadCount) {

        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certifiedProductDAO = certifiedProductDAO;
        this.curesListingStatisticsByAcbDAO = curesListingStatisticsByAcbDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.threadCount = threadCount;

        populateCuresCriteria(certificationCriterionService);

        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
    }

    @Transactional
    public void setCuresListingStatisticsForDate(LocalDate statisticDate) {
        if (hasStatisticsForDate(statisticDate)) {
            deleteStatisticsForDate(statisticDate);
        }
        List<CuresListingStatisticByAcb> stats = calculate(statisticDate);
        save(stats);
    }

    private List<CuresListingStatisticByAcb> calculate(LocalDate statisticDate) {
        List<CertificationBody> activeAcbs = certificationBodyDAO.findAllActive().stream()
                .map(dto -> new CertificationBody(dto))
                .collect(Collectors.toList());

        ForkJoinPool pool = new ForkJoinPool(threadCount);
        try {
            List<CertifiedProductSearchDetails> listings = pool.submit(() -> get2015ListingDetails()).get();

            return activeAcbs.stream()
                    .map(acb -> getCuresListingByAcbStatistic(listings, acb, statisticDate))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.catching(e);
            return new ArrayList<CuresListingStatisticByAcb>();
        }

    }

    private Boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CuresListingStatisticByAcb> statisticsForDate = curesListingStatisticsByAcbDAO.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    private void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CuresListingStatisticByAcb> statisticsForDate = curesListingStatisticsByAcbDAO.getStatisticsForDate(statisticDate);
        for (CuresListingStatisticByAcb statistic : statisticsForDate) {
            try {
                curesListingStatisticsByAcbDAO.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }

    private void save(List<CuresListingStatisticByAcb> statistics) {
        statistics.stream()
            .forEach(stat -> stat.setLastModifiedUser(User.SYSTEM_USER_ID));

        curesListingStatisticsByAcbDAO.create(statistics);
    }

    private CuresListingStatisticByAcb getCuresListingByAcbStatistic(List<CertifiedProductSearchDetails> listings, CertificationBody acb, LocalDate statisticDate) {
        CuresListingStatisticByAcb stat = new CuresListingStatisticByAcb();
        stat.setCertificationBody(acb);
        stat.setCuresListingWithoutCuresCriteriaCount(calculateCuresUpdateListingsWithoutCuresCriteriaCount(listings, acb));
        stat.setCuresListingWithCuresCriteriaCount(calculateCuresUpdatedListingsWithCuresCriteriaCount(listings, acb));
        stat.setNonCuresListingCount(calculateNonCuresListingCount(listings, acb));
        stat.setStatisticDate(statisticDate);
        return stat;
    }

    private Long calculateCuresUpdateListingsWithoutCuresCriteriaCount(List<CertifiedProductSearchDetails> listings, CertificationBody acb) {
        return listings.stream()
                .filter(listing -> Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()).equals(acb.getId()))
                .filter(listing -> listing.getCuresUpdate()
                        && !doesListingAttestToAnyCuresCriteria(listing))
                .collect(Collectors.counting());
    }

    private Long calculateCuresUpdatedListingsWithCuresCriteriaCount(List<CertifiedProductSearchDetails> listings, CertificationBody acb) {
        return listings.stream()
                .filter(listing -> Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()).equals(acb.getId()))
                .filter(listing -> listing.getCuresUpdate()
                        && doesListingAttestToAnyCuresCriteria(listing))
                .collect(Collectors.counting());
    }

    private Long calculateNonCuresListingCount(List<CertifiedProductSearchDetails> listings, CertificationBody acb) {
        return listings.stream()
                .filter(listing -> Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()).equals(acb.getId()))
                .filter(listing -> !listing.getCuresUpdate())
                .collect(Collectors.counting());
    }

    List<CertifiedProductSearchDetails> get2015ListingDetails() {
        return getAll2015CertifiedProducts().parallelStream()
                .filter(dto -> isListingActive(dto.getCertificationStatusName()))
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

    private Boolean isCuresCriterion(CertificationCriterion criterion) {
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

    private Boolean isListingActive(String certificationStatusName) {
        return activeStatusNames.stream()
                .filter(statusName -> statusName.equals(certificationStatusName))
                .findAny()
                .isPresent();
    }

}
