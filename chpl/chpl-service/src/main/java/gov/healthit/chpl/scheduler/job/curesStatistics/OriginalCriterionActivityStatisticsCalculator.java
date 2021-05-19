package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.dto.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class OriginalCriterionActivityStatisticsCalculator {
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticDao;

    @Autowired
    public OriginalCriterionActivityStatisticsCalculator(CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticDao) {
        this.criterionUpgradedToCuresFromOriginalStatisticDao = criterionUpgradedToCuresFromOriginalStatisticDao;
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public CriterionUpgradedToCuresFromOriginalListingStatisticDTO calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating original criteria upgraded to cures statistics for " + statisticDate);
        //TODO:
        return null;
    }

    @Transactional
    public void save(CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic) {
        try {
            criterionUpgradedToCuresFromOriginalStatisticDao.create(statistic);
        } catch (Exception ex) {
            LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                    + ", criterionId: " + statistic.getCriterion().getId()
                    + ", listingCount: " + statistic.getListingsUpgradedFromOriginalCount());
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatisticDTO> statisticsForDate
            = criterionUpgradedToCuresFromOriginalStatisticDao.getStatisticsForDate(statisticDate);
        for (CriterionUpgradedToCuresFromOriginalListingStatisticDTO statistic : statisticsForDate) {
            try {
                criterionUpgradedToCuresFromOriginalStatisticDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
