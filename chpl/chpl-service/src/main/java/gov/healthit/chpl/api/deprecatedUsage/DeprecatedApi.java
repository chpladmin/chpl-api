package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;

import org.springframework.http.HttpMethod;

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
    private HttpMethod httpMethod;
    private String apiOperation;
    private String requestParameter;
    private String responseField;
    private String changeDescription;
    private LocalDate removalDate;
}
