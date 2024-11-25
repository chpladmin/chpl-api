package gov.healthit.chpl.report.directreview;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectReviewCounts {
    private Long totalDirectReviewActivities;
    private Long openDirectReviewActivities;
    private Long closedDirectReviewActivities;
    private Long averageDurationToCloseDirectReview;

    private Long totalDirectReviewNonconformities;
    private Long openDirectReviewNonconformities;
    private Long closedDirectReviewNonconformities;

    private Long openDirectReviewCaps;
    private Long closedDirectReviewCaps;
}
