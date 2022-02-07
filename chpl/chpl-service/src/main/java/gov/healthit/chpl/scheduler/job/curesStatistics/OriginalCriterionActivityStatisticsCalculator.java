package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class OriginalCriterionActivityStatisticsCalculator {
    private CertificationCriterionService criteriaService;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;
    private CertifiedProductDAO certifiedProductDao;
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao;
    private Date curesEffectiveDate;
    private List<CertificationStatusType> activeStatuses;

    @Autowired
    public OriginalCriterionActivityStatisticsCalculator(CertificationCriterionService criteriaService,
            CertificationResultActivityHistoryHelper activityStatisticsHelper,
            CertifiedProductDAO certifiedProductDao,
            CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao,
            SpecialProperties specialProperties) {
        this.criteriaService = criteriaService;
        this.activityStatisticsHelper = activityStatisticsHelper;
        this.certifiedProductDao = certifiedProductDao;
        this.criterionUpgradedToCuresFromOriginalStatisticsDao = criterionUpgradedToCuresFromOriginalStatisticsDao;
        curesEffectiveDate = specialProperties.getEffectiveRuleTimestamp();
        activeStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setOriginalCriterionActivityStatisticsForDate(LocalDate statisticDate) {
        if (hasStatisticsForDate(statisticDate)) {
            deleteStatisticsForDate(statisticDate);
        }
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> currentStatistics = calculateCurrentStatistics(statisticDate);
        save(currentStatistics);
    }

    private boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    private List<CriterionUpgradedToCuresFromOriginalListingStatistic> calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating original criteria upgraded to cures statistics for " + statisticDate);
        Date currentDate = new Date();
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> results
            = new ArrayList<CriterionUpgradedToCuresFromOriginalListingStatistic>();

        Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
        for (CertificationCriterion originalCriterion : originalToCuresCriteriaMap.keySet()) {
            long listingCount = 0;
            CertificationCriterion curesCriterion = originalToCuresCriteriaMap.get(originalCriterion);
            List<Long> listingIdsAttestingToCriterion = certifiedProductDao.getListingIdsAttestingToCriterion(curesCriterion.getId(), activeStatuses);
            if (!CollectionUtils.isEmpty(listingIdsAttestingToCriterion)) {
                LOGGER.debug("Listing IDs attesting to criterion ID " + curesCriterion.getId() + ": "
                        + listingIdsAttestingToCriterion.stream()
                            .map(listingId -> listingId.toString())
                            .collect(Collectors.joining(",")));
            } else {
                LOGGER.debug("No listings attest to criterion ID " + curesCriterion.getId());
            }

            for (Long listingId : listingIdsAttestingToCriterion) {
                LOGGER.debug("Checking if listing ID " + listingId
                        + " removed attestation to original criterion ID " + originalCriterion.getId()
                        + " between " + curesEffectiveDate + " and " + currentDate);
                if (activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listingId, originalCriterion, curesEffectiveDate, currentDate)) {
                    listingCount++;
                }
            }
            results.add(buildStatistic(curesCriterion, listingCount, statisticDate));
        }
        return results;
    }

    private CriterionUpgradedToCuresFromOriginalListingStatistic buildStatistic(
            CertificationCriterion criterion, long listingCount, LocalDate statisticDate) {
        return CriterionUpgradedToCuresFromOriginalListingStatistic.builder()
                .listingsUpgradedFromOriginalCount(listingCount)
                .curesCriterion(CertificationCriterionDTO.builder()
                        .id(criterion.getId())
                        .number(criterion.getNumber())
                        .title(criterion.getTitle())
                        .build())
                .statisticDate(statisticDate)
                .build();
    }

    private void save(List<CriterionUpgradedToCuresFromOriginalListingStatistic> statistics) {
        for (CriterionUpgradedToCuresFromOriginalListingStatistic statistic : statistics) {
            try {
                criterionUpgradedToCuresFromOriginalStatisticsDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                        + ", criterionId: " + statistic.getCuresCriterion().getId()
                        + ", listingCount: " + statistic.getListingsUpgradedFromOriginalCount());
            }
        }
    }

    private void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticsDao.getStatisticsForDate(statisticDate);
        for (CriterionUpgradedToCuresFromOriginalListingStatistic statistic : statisticsForDate) {
            try {
                criterionUpgradedToCuresFromOriginalStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
