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
import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class OriginalCriterionActivityStatisticsCalculator {
    private CertificationCriterionService criteriaService;
    private CriterionActivityStatisticsHelper activityStatisticsHelper;
    private CertifiedProductDAO certifiedProductDao;
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao;
    private Date curesEffectiveDate;
    private Date currentDate;
    private List<CertificationStatusType> activeStatuses;

    @Autowired
    public OriginalCriterionActivityStatisticsCalculator(CertificationCriterionService criteriaService,
            CriterionActivityStatisticsHelper activityStatisticsHelper,
            CertifiedProductDAO certifiedProductDao,
            CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao,
            SpecialProperties specialProperties) {
        this.criteriaService = criteriaService;
        this.activityStatisticsHelper = activityStatisticsHelper;
        this.certifiedProductDao = certifiedProductDao;
        this.criterionUpgradedToCuresFromOriginalStatisticsDao = criterionUpgradedToCuresFromOriginalStatisticsDao;
        curesEffectiveDate = specialProperties.getEffectiveRuleTimestamp();
        currentDate = new Date();
        activeStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating original criteria upgraded to cures statistics for " + statisticDate);
        List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> results
            = new ArrayList<CriterionUpgradedToCuresFromOriginalListingStatisticDTO>();

        Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap = criteriaService.getOriginalToCuresCriteriaMap();
        for (CertificationCriterion originalCriterion : originalToCuresCriteriaMap.keySet()) {
            long listingCount = 0;
            CertificationCriterion curesCriterion = originalToCuresCriteriaMap.get(originalCriterion);
            List<Long> listingIdsAttestingToCriterion = certifiedProductDao.getListingIdsAttestingToCriterion(curesCriterion.getId(), activeStatuses);
            for (Long listingId : listingIdsAttestingToCriterion) {
                if (activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listingId, originalCriterion, curesEffectiveDate, currentDate)) {
                    listingCount++;
                }
            }
            results.add(buildStatistic(curesCriterion, listingCount, statisticDate));
        }
        return results;
    }

    private CriterionUpgradedToCuresFromOriginalListingStatisticDTO buildStatistic(
            CertificationCriterion criterion, long listingCount, LocalDate statisticDate) {
        return CriterionUpgradedToCuresFromOriginalListingStatisticDTO.builder()
                .listingsUpgradedFromOriginalCount(listingCount)
                .curesCriterion(CertificationCriterionDTO.builder()
                        .id(criterion.getId())
                        .number(criterion.getNumber())
                        .title(criterion.getTitle())
                        .build())
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statistics) {
        for (CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic : statistics) {
            try {
                criterionUpgradedToCuresFromOriginalStatisticsDao.create(statistic);
            } catch (Exception ex) {
                LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                        + ", criterionId: " + statistic.getCuresCriterion().getId()
                        + ", listingCount: " + statistic.getListingsUpgradedFromOriginalCount());
            }
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticsDao.getStatisticsForDate(statisticDate);
        for (CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic : statisticsForDate) {
            try {
                criterionUpgradedToCuresFromOriginalStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
