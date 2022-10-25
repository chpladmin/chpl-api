package gov.healthit.chpl.manager.rules.developer;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Data;

@Data
public class DeveloperValidationContext {
    private Developer developer;
    private ErrorMessageUtil errorMessageUtil;
    private Developer beforeDev;
    private List<Developer> beforeDevs;

    public DeveloperValidationContext(Developer developer, ErrorMessageUtil errorMessageUtil) {
        this.developer = developer;
        this.errorMessageUtil = errorMessageUtil;
        this.beforeDev = null;
        this.beforeDevs = new ArrayList<Developer>();
    }

    public DeveloperValidationContext(Developer developer, ErrorMessageUtil errorMessageUtil, Developer beforeDev) {
        this(developer, errorMessageUtil);
        this.beforeDev = beforeDev;
    }

    public DeveloperValidationContext(Developer developer, ErrorMessageUtil errorMessageUtil, List<Developer> beforeDevs) {
        this(developer, errorMessageUtil);
        this.beforeDevs = beforeDevs;
    }
}
