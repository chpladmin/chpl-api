package gov.healthit.chpl.domain.statistics;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuresCriterionChartStatistic {
    private CertificationCriterion criterion;
    private Long existingCertificationCount;
    private Long newCertificationCount;
    private Long listingCount;
    private Long requiresUpdateCount;
}
