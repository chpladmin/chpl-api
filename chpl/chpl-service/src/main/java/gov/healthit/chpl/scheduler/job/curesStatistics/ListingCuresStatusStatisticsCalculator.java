package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.ListingCuresStatusStatisticsDAO;
import gov.healthit.chpl.dto.statistics.ListingCuresStatusStatisticDTO;
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
    public boolean hasStatisticsForDate(LocalDate statisticDate) {
        List<ListingCuresStatusStatisticDTO> statisticsForDate
            = listingCuresStatusStatisticsDao.getStatisticsForDate(statisticDate);
        return statisticsForDate != null && statisticsForDate.size() > 0;
    }

    @Transactional
    public ListingCuresStatusStatisticDTO calculateCurrentStatistics(LocalDate statisticDate) {
        Long curesListingsCount = listingCuresStatusStatisticsDao.getListingCountWithCuresUpdateStatus();
        Long totalListingsCount = listingCuresStatusStatisticsDao.getTotalListingCount();

        return ListingCuresStatusStatisticDTO.builder()
                .curesListingCount(curesListingsCount)
                .totalListingCount(totalListingsCount)
                .statisticDate(statisticDate)
                .build();
    }

    @Transactional
    public void save(ListingCuresStatusStatisticDTO statistic) {
        try {
            listingCuresStatusStatisticsDao.create(statistic);
        } catch (Exception ex) {
            LOGGER.error("Could not save statistic for date: " + statistic.getStatisticDate()
                    + ", curesListingCount: " + statistic.getCuresListingCount()
                    + ", totalListingCount: " + statistic.getTotalListingCount());
        }
    }

    @Transactional
    public void deleteStatisticsForDate(LocalDate statisticDate) {
        List<ListingCuresStatusStatisticDTO> statisticsForDate = listingCuresStatusStatisticsDao.getStatisticsForDate(statisticDate);
        for (ListingCuresStatusStatisticDTO statistic : statisticsForDate) {
            try {
                listingCuresStatusStatisticsDao.delete(statistic.getId());
            } catch (Exception ex) {
                LOGGER.error("Could not delete statistic with ID " + statistic.getId());
            }
        }
    }
}
