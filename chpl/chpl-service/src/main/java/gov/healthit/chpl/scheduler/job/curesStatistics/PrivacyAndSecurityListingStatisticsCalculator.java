package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.PrivacyAndSecurityListingStatisticsDAO;
import gov.healthit.chpl.dto.statistics.PrivacyAndSecurityListingStatisticDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class PrivacyAndSecurityListingStatisticsCalculator {
    private PrivacyAndSecurityListingStatisticsDAO privacyAndSecurityListingStatisticsDao;

    @Autowired
    public PrivacyAndSecurityListingStatisticsCalculator(PrivacyAndSecurityListingStatisticsDAO privacyAndSecurityListingStatisticsDao) {
        this.privacyAndSecurityListingStatisticsDao = privacyAndSecurityListingStatisticsDao;
    }

    @Transactional
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<PrivacyAndSecurityListingStatisticDTO> statisticsForDate
            = privacyAndSecurityListingStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public PrivacyAndSecurityListingStatisticDTO calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating privacy and security statistics for " + statisticDate);
        Long hasPrivacyAndSecurityCriteriaCount = privacyAndSecurityListingStatisticsDao.getListingCountWithPrivacyAndSecurityCriteria();
        LOGGER.info("Found " + hasPrivacyAndSecurityCriteriaCount + " listings with P&S criteria.");
        Long requiresPrivacyAndSecurityCriteriaCount = privacyAndSecurityListingStatisticsDao.getListingCountRequiringPrivacyAndSecurityCriteria();
        LOGGER.info("Found " + requiresPrivacyAndSecurityCriteriaCount + " listings requiring P&S criteria.");

        return PrivacyAndSecurityListingStatisticDTO.builder()
                .listingsWithPrivacyAndSecurityCount(hasPrivacyAndSecurityCriteriaCount)
                .listingsRequiringPrivacyAndSecurityCount(requiresPrivacyAndSecurityCriteriaCount)
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(PrivacyAndSecurityListingStatisticDTO statistic) {
        try {
            privacyAndSecurityListingStatisticsDao.create(statistic);
        } catch (Exception ex) {
            LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                    + ", hasPrivacyAndSecurityCriteriaCount: " + statistic.getListingsWithPrivacyAndSecurityCount()
                    + ", requiresPrivacyAndSecurityCriteriaCount: " + statistic.getListingsRequiringPrivacyAndSecurityCount());
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<PrivacyAndSecurityListingStatisticDTO> statisticsForDate = privacyAndSecurityListingStatisticsDao.getStatisticsForDate(statisticDate);
        for (PrivacyAndSecurityListingStatisticDTO statistic : statisticsForDate) {
            try {
                privacyAndSecurityListingStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
