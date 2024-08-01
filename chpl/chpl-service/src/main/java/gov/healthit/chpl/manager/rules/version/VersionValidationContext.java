package gov.healthit.chpl.manager.rules.version;

import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionValidationContext {
    private ProductVersionDAO versionDao;
    private ProductVersion version;
    private Long productId;
    private ErrorMessageUtil errorMessageUtil;
}
