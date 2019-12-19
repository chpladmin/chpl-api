package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperValidationContext {
    private DeveloperDTO DeveloperDTO;
    private ErrorMessageUtil errorMessageUtil;
    private final String pendingAcbName;
    private final DeveloperDTO beforeDev;

    public DeveloperValidationContext(final DeveloperDTO DeveloperDTO, final ErrorMessageUtil errorMessageUtil) {
        this(DeveloperDTO, errorMessageUtil, null);
    }

    public DeveloperValidationContext(final DeveloperDTO DeveloperDTO, final ErrorMessageUtil errorMessageUtil,
            final String pendingAcbName) {
        this(DeveloperDTO, errorMessageUtil, pendingAcbName, null);
    }

    public DeveloperValidationContext(final DeveloperDTO DeveloperDTO, final ErrorMessageUtil errorMessageUtil,
            final String pendingAcbName, final DeveloperDTO beforeDev) {
        this.DeveloperDTO = DeveloperDTO;
        this.errorMessageUtil = errorMessageUtil;
        this.pendingAcbName = pendingAcbName;
        this.beforeDev = beforeDev;
    }

    public DeveloperDTO getDeveloperDTO() {
        return DeveloperDTO;
    }

    public void setDeveloperDTO(DeveloperDTO DeveloperDTO) {
        this.DeveloperDTO = DeveloperDTO;
    }

    public ErrorMessageUtil getErrorMessageUtil() {
        return errorMessageUtil;
    }

    public void setErrorMessageUtil(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public String getPendingAcbName() {
        return pendingAcbName;
    }

    public final DeveloperDTO getBeforeDev() {
        return beforeDev;
    }
}
