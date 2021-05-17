package gov.healthit.chpl.dto.statistics;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacyAndSecurityListingStatisticDTO {
    private Long id;
    private LocalDate statisticDate;
    private Long listingsWithPrivacyAndSecurityCount;
    private Long listingsRequiringPrivacyAndSecurityCount;
}
