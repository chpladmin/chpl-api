package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.compliance.DirectReview;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DirectReviewComparator implements Comparator<DirectReview> {

    @Override
    public int compare(DirectReview dr1, DirectReview dr2) {
        if (dr1.getLastUpdated() != null && dr2.getLastUpdated() != null) {
            return dr1.getLastUpdated().compareTo(dr2.getLastUpdated());
        }
        return 0;
    }
}
