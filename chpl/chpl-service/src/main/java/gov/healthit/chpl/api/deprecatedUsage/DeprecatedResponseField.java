package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeprecatedResponseField {
    private Long id;
    private String responseField;
    private String changeDescription;
    private LocalDate removalDate;
}
