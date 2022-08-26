package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;
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
public class DeprecatedApiUsage {
    private Long id;
    private ApiKey apiKey;

    private String httpMethod;
    private String apiOperation;
    private String responseField;

    private LocalDate removalDate;
    private String message;
    private Long callCount;
    private Date lastAccessedDate;
    private Date notificationSent;

    public String getEndpoint() {
        return this.getHttpMethod() + ":" + this.getApiOperation();
    }
}
