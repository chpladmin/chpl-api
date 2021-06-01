package gov.healthit.chpl.domain.statistics;

import java.time.LocalDate;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuresCriterionUpgradedWithoutOriginalListingStatistic {
    private Long id;
    private LocalDate statisticDate;
    private CertificationCriterionDTO curesCriterion;
    private Long listingsUpgradedWithoutAttestingToOriginalCount;
}
