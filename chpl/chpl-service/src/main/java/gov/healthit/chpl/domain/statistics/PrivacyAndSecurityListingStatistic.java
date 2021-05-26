package gov.healthit.chpl.domain.statistics;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacyAndSecurityListingStatistic {
    private Long id;
    private LocalDate statisticDate;
    private Long listingsWithPrivacyAndSecurityCount;
    private Long listingsRequiringPrivacyAndSecurityCount;
}
