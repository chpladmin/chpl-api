package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
public class DirectReviewDataCalculator {
    private DirectReviewSearchService directReviewSearchService;

    @Autowired
    public DirectReviewDataCalculator(DirectReviewSearchService directReviewSearchService) {
        this.directReviewSearchService = directReviewSearchService;
    }

    public Long getTotalDirectReviews() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            int totalDrs = directReviewSearchService.getAll().stream()
                .filter(drContainer -> !CollectionUtils.isEmpty(drContainer.getDirectReviews()))
                .map(drContainer -> drContainer.getDirectReviews().size())
                .reduce(0, Integer::sum);
            return Long.valueOf(totalDrs);
        } else {
            return null;
        }
    }

    public Long getOpenDirectReviews() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .filter(dr -> isDirectReviewOpen(dr))
                    .collect(Collectors.counting());
        } else {
            return null;
        }
    }

    public Long getClosedDirectReviews() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .filter(dr -> !isDirectReviewOpen(dr))
                    .collect(Collectors.counting());
        } else {
            return null;
        }
    }

    public Long getAverageTimeToCloseDirectReview() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .filter(dr -> !isDirectReviewOpen(dr))
                    .collect(Collectors.averagingLong(dr -> getDirectReviewDaysOpen(dr)))
                    .longValue();
        } else {
            return null;
        }
    }

    public Long getTotalNonConformities() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
        return directReviewSearchService.getAll().stream()
                .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                .flatMap(dr -> dr.getNonConformities().stream())
                .count();
        } else {
            return null;
        }
    }

    public Long getOpenNonConformities() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
        return directReviewSearchService.getAll().stream()
                .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getNonConformityStatus().equalsIgnoreCase(DirectReviewNonConformity.STATUS_OPEN))
                .count();
        } else {
            return null;
        }
    }

    public Long getClosedNonConformities() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .flatMap(dr -> dr.getNonConformities().stream())
                    .filter(nc -> nc.getNonConformityStatus().equalsIgnoreCase(DirectReviewNonConformity.STATUS_CLOSED))
                    .count();
        } else {
            return null;
        }
    }

    public Long getOpenCaps() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .flatMap(dr -> dr.getNonConformities().stream())
                    .filter(nc -> nc.getCapApprovalDate() != null && nc.getCapEndDate() == null)
                    .count();
        } else {
            return null;
        }
    }

    public Long getClosedCaps() {
        if (directReviewSearchService.doesCacheHaveAnyOkData()) {
            return directReviewSearchService.getAll().stream()
                    .flatMap(drContainer -> drContainer.getDirectReviews().stream())
                    .flatMap(dr -> dr.getNonConformities().stream())
                    .filter(nc -> nc.getCapApprovalDate() != null && nc.getCapEndDate() != null)
                    .count();
        } else {
            return null;
        }
    }

    private Boolean isDirectReviewOpen(DirectReview dr) {
        return dr.getEndDate() == null;
    }

    private Long getDirectReviewDaysOpen(DirectReview dr) {
        return Math.abs(ChronoUnit.DAYS.between(dr.getStartDate().toInstant(),
                dr.getEndDate().toInstant()));
    }
}
