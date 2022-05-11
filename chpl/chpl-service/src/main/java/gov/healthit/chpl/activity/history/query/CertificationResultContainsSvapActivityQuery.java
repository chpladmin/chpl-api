package gov.healthit.chpl.activity.history.query;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CertificationResultContainsSvapActivityQuery extends ListingActivityQuery {
    private CertificationCriterion criterion;
    private Svap svap;

    @Builder
    public CertificationResultContainsSvapActivityQuery(Long listingId, Svap svap,
            CertificationCriterion criterion) {
        super(listingId);
        this.svap = svap;
        this.criterion = criterion;
    }
}
