package gov.healthit.chpl.report.directreview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.search.ListingSearchService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DirectReviewReportsService {
    private SummaryStatisticsDAO summaryStatisticsDAO;

    @Autowired
    public DirectReviewReportsService(SummaryStatisticsDAO summaryStatisticsDAO, ListingSearchService listingSearchService) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
    }

    public DirectReviewCounts getDirectReviewCounts() {
        StatisticsSnapshot stats = getStatistics();
        return DirectReviewCounts.builder()
                .totalDirectReviewActivities(stats.getTotalDirectReviews())
                .openDirectReviewActivities(stats.getOpenDirectReviews())
                .closedDirectReviewActivities(stats.getClosedDirectReviews())
                .averageDurationToCloseDirectReview(stats.getAverageDaysOpenDirectReviews())
                .totalDirectReviewNonconformities(stats.getTotalNonConformities())
                .closedDirectReviewNonconformities(stats.getClosedNonConformities())
                .openDirectReviewNonconformities(stats.getOpenNonConformities())
                .closedDirectReviewCaps(stats.getClosedCaps())
                .openDirectReviewCaps(stats.getOpenCaps())
                .build();
    }

    private StatisticsSnapshot getStatistics() {
        try {
            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(summaryStatistics.getSummaryStatistics(), StatisticsSnapshot.class);
        } catch (Exception e) {
            LOGGER.error("Error retrieving summary statistics: {}", e.getMessage());
            return null;
        }
    }
}
