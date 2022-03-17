package gov.healthit.chpl.manager.rules.developer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperPriorStatusActiveValidation extends ValidationRule<DeveloperValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperPriorStatusActiveValidation.class);
    private ResourcePermissions resourcePermissions;

    public DeveloperPriorStatusActiveValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer beforeDev = context.getBeforeDev();
        DeveloperStatus currDevStatus = beforeDev.getStatus();
        if (!currDevStatus.getStatus().equals(DeveloperStatusType.Active.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.notActiveNotAdminCantChangeStatus", AuthUtil.getUsername(),
                    beforeDev.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
