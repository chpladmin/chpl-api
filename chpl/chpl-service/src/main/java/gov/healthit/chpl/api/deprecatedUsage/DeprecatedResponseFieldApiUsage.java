package gov.healthit.chpl.api.deprecatedUsage;

import java.util.Date;

import gov.healthit.chpl.api.domain.ApiKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeprecatedResponseFieldApiUsage {
    private Long id;
    private DeprecatedResponseFieldApi api;
    private ApiKey apiKey;
    private Long callCount;
    private Date lastAccessedDate;
}
