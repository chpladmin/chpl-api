package gov.healthit.chpl.dto.statistics;

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
public class ListingToCriterionForCuresAchievementStatisticDTO {
    private Long id;
    private LocalDate statisticDate;
    private CertificationCriterionDTO criterion;
    private Long listingId;
}
