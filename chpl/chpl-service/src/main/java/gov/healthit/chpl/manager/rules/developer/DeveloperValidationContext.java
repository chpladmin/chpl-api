package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Data;

@Data
public class DeveloperValidationContext {
    private Developer developer;
    private ErrorMessageUtil errorMessageUtil;
    private Developer beforeDev;

    public DeveloperValidationContext(Developer developer, ErrorMessageUtil errorMessageUtil) {
        this(developer, errorMessageUtil, null);
    }

    public DeveloperValidationContext(Developer developer, ErrorMessageUtil errorMessageUtil, Developer beforeDev) {
        this.developer = developer;
        this.errorMessageUtil = errorMessageUtil;
        this.beforeDev = beforeDev;
    }
}
