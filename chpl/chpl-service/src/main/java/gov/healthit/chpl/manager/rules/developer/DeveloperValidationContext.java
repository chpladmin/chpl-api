package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Data;

@Data
public class DeveloperValidationContext {
    private DeveloperDTO developerDTO;
    private ErrorMessageUtil errorMessageUtil;
    private DeveloperDTO beforeDev;

    public DeveloperValidationContext(DeveloperDTO developerDTO, ErrorMessageUtil errorMessageUtil) {
        this(developerDTO, errorMessageUtil, null);
    }

    public DeveloperValidationContext(DeveloperDTO developerDTO, ErrorMessageUtil errorMessageUtil, DeveloperDTO beforeDev) {
        this.developerDTO = developerDTO;
        this.errorMessageUtil = errorMessageUtil;
        this.beforeDev = beforeDev;
    }
}
