package gov.healthit.chpl.domain.statistics;

import java.time.LocalDate;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingToCriterionForCuresAchievementStatistic {
    private Long id;
    private LocalDate statisticDate;
    private CertificationCriterion criterion;
    private Long listingId;
}
