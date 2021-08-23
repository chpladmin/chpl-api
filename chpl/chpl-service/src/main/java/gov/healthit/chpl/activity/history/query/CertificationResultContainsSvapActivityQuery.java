package gov.healthit.chpl.activity.history.query;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CertificationResultContainsSvapActivityQuery implements ListingActivityQuery {
    private Long listingId;
    private CertificationCriterion criterion;
    private Svap svap;
}
