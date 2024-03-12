package gov.healthit.chpl.manager.rules.developer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperPriorStatusActiveValidation extends ValidationRule<DeveloperValidationContext> {
    private static final Logger LOGGER = LogManager.getLogger(DeveloperPriorStatusActiveValidation.class);
    private ResourcePermissionsFactory resourcePermissionsFactory;

    public DeveloperPriorStatusActiveValidation(ResourcePermissionsFactory resourcePermissionsFactory) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer beforeDev = context.getBeforeDev();
        DeveloperStatus currDevStatus = beforeDev.getStatus();
        if (!currDevStatus.getStatus().equals(DeveloperStatusType.Active.toString())
                && !resourcePermissionsFactory.get().isUserRoleAdmin() && !resourcePermissionsFactory.get().isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.notActiveNotAdminCantChangeStatus", AuthUtil.getUsername(),
                    beforeDev.getName());
            getMessages().add(msg);
            LOGGER.error(msg);
            return false;
        }
        return true;
    }
}
