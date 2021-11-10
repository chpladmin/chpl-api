package gov.healthit.chpl.api.deprecatedUsage;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeprecatedResponseFieldApi {
    private Long id;
    private ApiOperation apiOperation;
    private List<DeprecatedResponseField> responseFields;
}
