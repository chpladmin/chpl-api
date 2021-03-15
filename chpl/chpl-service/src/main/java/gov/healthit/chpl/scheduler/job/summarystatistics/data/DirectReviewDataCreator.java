package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.service.DirectReviewSearchService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
public class DirectReviewDataCreator extends StatisticsDataCreator  {
    private static final String DR_NON_CONFIRMITY_STATUS_OPEN = "Open";
    private DirectReviewSearchService directReviewSearchService;

    @Autowired
    public DirectReviewDataCreator(DirectReviewSearchService directReviewSearchService) {
        this.directReviewSearchService = directReviewSearchService;
    }

    public Long getTotalDirectReviews() {
        return Long.valueOf(directReviewSearchService.getAll().size());
    }

    public long getOpenDirectReviews() {
        return directReviewSearchService.getAll().stream()
                .filter(dr -> isDirectReviewOpen(dr))
                .collect(Collectors.counting());

    }

    public long getClosedDirectReviews() {
        return directReviewSearchService.getAll().stream()
                .filter(dr -> !isDirectReviewOpen(dr))
                .collect(Collectors.counting());
    }

    public Long getAverageTimeToCloseDirectReview() {
        return directReviewSearchService.getAll().stream()
                .filter(dr -> !isDirectReviewOpen(dr))
                .collect(Collectors.averagingLong(dr -> getDirectReviewDaysOpen(dr)))
                .longValue();
    }

    public Long getTotaNonConformities() {
        return directReviewSearchService.getAll().stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .count();
    }

    public Long getOpenNonConformities() {
        return directReviewSearchService.getAll().stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getNonConformityStatus().equals(DR_NON_CONFIRMITY_STATUS_OPEN))
                .count();
    }

    public Long getClosedNonConformities() {
        return directReviewSearchService.getAll().stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> !nc.getNonConformityStatus().equals(DR_NON_CONFIRMITY_STATUS_OPEN))
                .count();
    }

    public Long getOpenCaps() {
        return directReviewSearchService.getAll().stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getCapApprovalDate() != null && nc.getCapEndDate() == null)
                .count();
    }

    public Long getClosedCaps() {
        return directReviewSearchService.getAll().stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getCapApprovalDate() != null && nc.getCapEndDate() != null)
                .count();
    }

    private Boolean isDirectReviewOpen(DirectReview dr) {
        return dr.getEndDate() == null;
    }

    private Long getDirectReviewDaysOpen(DirectReview dr) {
        return Math.abs(ChronoUnit.DAYS.between(dr.getStartDate().toInstant(),
                dr.getEndDate().toInstant()));
    }
}
