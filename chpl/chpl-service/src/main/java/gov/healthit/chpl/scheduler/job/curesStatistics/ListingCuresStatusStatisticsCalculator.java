package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.ListingCuresStatusStatisticsDAO;
import gov.healthit.chpl.domain.statistics.ListingCuresStatusStatistic;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsCreatorJobLogger")
public class ListingCuresStatusStatisticsCalculator {
    private ListingCuresStatusStatisticsDAO listingCuresStatusStatisticsDao;

    @Autowired
    public ListingCuresStatusStatisticsCalculator(ListingCuresStatusStatisticsDAO listingCuresStatusStatisticsDao) {
        this.listingCuresStatusStatisticsDao = listingCuresStatusStatisticsDao;
    }

    @Transactional
    public void setListingCuresStatusStatisticsForDate(LocalDate statisticDate) {
        if (hasStatisticsForDate(statisticDate)) {
            deleteStatisticsForDate(statisticDate);
        }
        ListingCuresStatusStatistic currentStatistic = calculateCurrentStatistics(statisticDate);
        save(currentStatistic);
    }

    private boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<ListingCuresStatusStatistic> statisticsForDate
            = listingCuresStatusStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    private ListingCuresStatusStatistic calculateCurrentStatistics(LocalDate statisticDate) {
        LOGGER.info("Calculating cures status statistics for " + statisticDate);
        Long curesListingsCount = listingCuresStatusStatisticsDao.getListingCountWithCuresUpdateStatus();
        LOGGER.info("Found " + curesListingsCount + " listings with Cures designation.");
        Long totalListingsCount = listingCuresStatusStatisticsDao.getTotalListingCount();
        LOGGER.info("Found " + totalListingsCount + " total listings.");

        return ListingCuresStatusStatistic.builder()
                .curesListingCount(curesListingsCount)
                .totalListingCount(totalListingsCount)
                .statisticDate(statisticDate)
                .build();
    }

    private void save(ListingCuresStatusStatistic statistic) {
        try {
            listingCuresStatusStatisticsDao.create(statistic);
        } catch (Exception ex) {
            LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                    + ", curesListingCount: " + statistic.getCuresListingCount()
                    + ", totalListingCount: " + statistic.getTotalListingCount());
        }
    }

    private void deleteStatisticsForDate(LocalDate statisticDate) {
        List<ListingCuresStatusStatistic> statisticsForDate = listingCuresStatusStatisticsDao.getStatisticsForDate(statisticDate);
        for (ListingCuresStatusStatistic statistic : statisticsForDate) {
            try {
                listingCuresStatusStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
