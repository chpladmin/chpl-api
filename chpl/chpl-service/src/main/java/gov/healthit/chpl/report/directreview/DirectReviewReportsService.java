package gov.healthit.chpl.report.directreview;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DirectReviewReportsService {
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private DirectReviewSearchService drService;

    @Autowired
    public DirectReviewReportsService(SummaryStatisticsDAO summaryStatisticsDAO, DirectReviewSearchService drService) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.drService = drService;
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

    public List<DirectReview> getDirectReviews() {
        return drService.getAll().stream()
                .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                .collect(Collectors.toList());
    }

    private StatisticsSnapshot getStatistics() {
        return summaryStatisticsDAO.getCurrentSummaryStatistics();
    }
}
