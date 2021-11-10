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
public class DeprecatedApi {
    private Long id;
    private ApiOperation apiOperation;
    private String requestParameter;
    private String changeDescription;
    private LocalDate removalDate;
}
