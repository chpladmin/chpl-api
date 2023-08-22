package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriterionUpgradedWithoutOriginalListingStatistic;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class CuresCriterionActivityStatisticsCalculator {
    private CertificationCriterionService criteriaService;
    private CertifiedProductDAO certifiedProductDao;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;
    private CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalStatisticDao;
    private Date curesEffectiveDate;
    private List<CertificationStatusType> activeStatuses;

    @Autowired
    public CuresCriterionActivityStatisticsCalculator(CertificationCriterionService criteriaService,
            CertificationResultActivityHistoryHelper activityStatisticsHelper,
            CertifiedProductDAO certifiedProductDao,
            CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalStatisticDao,
            SpecialProperties specialProperties) {
        this.criteriaService = criteriaService;
        this.activityStatisticsHelper = activityStatisticsHelper;
        this.certifiedProductDao = certifiedProductDao;
        this.curesCriterionUpgradedWithoutOriginalStatisticDao = curesCriterionUpgradedWithoutOriginalStatisticDao;
        curesEffectiveDate = specialProperties.getEffectiveRuleTimestamp();
        activeStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setCuresCriterionActivityStatisticsForDate(LocalDate statisticDate) {
        if (hasStatisticsForDate(statisticDate)) {
            deleteStatisticsForDate(statisticDate);
        }
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> currentStatistics = calculateCurrentStatistics(statisticDate);
        save(currentStatistics);
    }

    private boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> statisticsForDate
            = curesCriterionUpgradedWithoutOriginalStatisticDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    private List<CuresCriterionUpgradedWithoutOriginalListingStatistic> calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating cures criterion upgrade without original statistics for " + statisticDate);
        Date currentDate = new Date();
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> results
            = new ArrayList<CuresCriterionUpgradedWithoutOriginalListingStatistic>();

        Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
        for (CertificationCriterion originalCriterion : originalToCuresCriteriaMap.keySet()) {
            long listingCount = 0;
            CertificationCriterion curesCriterion = originalToCuresCriteriaMap.get(originalCriterion);
            List<Long> listingIdsAttestingToCriterion = certifiedProductDao.getListingIdsAttestingToCriterion(curesCriterion.getId(), activeStatuses);
            for (Long listingId : listingIdsAttestingToCriterion) {
                if (!activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listingId, originalCriterion, curesEffectiveDate, currentDate)) {
                    listingCount++;
                }
            }
            results.add(buildStatistic(curesCriterion, listingCount, statisticDate));
        }
        return results;
    }

    private CuresCriterionUpgradedWithoutOriginalListingStatistic buildStatistic(
            CertificationCriterion criterion, long listingCount, LocalDate statisticDate) {
        return CuresCriterionUpgradedWithoutOriginalListingStatistic.builder()
                .listingsUpgradedWithoutAttestingToOriginalCount(listingCount)
                .curesCriterion(CertificationCriterion.builder()
                        .id(criterion.getId())
                        .number(criterion.getNumber())
                        .title(criterion.getTitle())
                        .build())
                .statisticDate(statisticDate)
                .build();
    }

    private void save(List<CuresCriterionUpgradedWithoutOriginalListingStatistic> statistics) {
        for (CuresCriterionUpgradedWithoutOriginalListingStatistic statistic : statistics) {
            try {
                curesCriterionUpgradedWithoutOriginalStatisticDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                        + ", criterionId: " + statistic.getCuresCriterion().getId()
                        + ", listingCount: " + statistic.getListingsUpgradedWithoutAttestingToOriginalCount());
            }
        }
    }

    private void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> statisticsForDate
            = curesCriterionUpgradedWithoutOriginalStatisticDao.getStatisticsForDate(statisticDate);
        for (CuresCriterionUpgradedWithoutOriginalListingStatistic statistic : statisticsForDate) {
            try {
                curesCriterionUpgradedWithoutOriginalStatisticDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
