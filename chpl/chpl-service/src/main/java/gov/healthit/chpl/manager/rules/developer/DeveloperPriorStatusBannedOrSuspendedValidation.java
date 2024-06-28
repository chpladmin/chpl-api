package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperPriorStatusBannedOrSuspendedValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissionsFactory resourcePermissionsFactory;

    public DeveloperPriorStatusBannedOrSuspendedValidation(ResourcePermissionsFactory resourcePermissionsFactory) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer beforeDev = context.getBeforeDev();
        DeveloperStatus currDevStatus = beforeDev.getCurrentStatusEvent() != null ? beforeDev.getCurrentStatusEvent().getStatus() : null;
        if (currDevStatus != null
                && !resourcePermissionsFactory.get().isUserRoleAdmin() && !resourcePermissionsFactory.get().isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.bannedOrSuspendedNotAdminCantChangeStatus", AuthUtil.getUsername(),
                    beforeDev.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
