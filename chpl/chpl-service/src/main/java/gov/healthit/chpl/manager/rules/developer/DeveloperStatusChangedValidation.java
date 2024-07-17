package gov.healthit.chpl.manager.rules.developer;

import java.util.Objects;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperStatusChangedValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissionsFactory resourcePermissionsFactory;

    public DeveloperStatusChangedValidation(ResourcePermissionsFactory resourcePermissionsFactory) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    public DeveloperStatusChangedValidation(DeveloperManager developerManagerImpl,
            ResourcePermissionsFactory resourcePermissionsFactory, DeveloperDAO developerDao) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer updatedDev = context.getDeveloper();
        Developer beforeDev = context.getBeforeDev();
        DeveloperStatus newDevStatus = updatedDev.getCurrentStatusEvent() != null ? updatedDev.getCurrentStatusEvent().getStatus() : null;
        DeveloperStatus currDevStatus = beforeDev.getCurrentStatusEvent() != null ? beforeDev.getCurrentStatusEvent().getStatus() : null;

        boolean currentStatusChanged = !Objects.equals(newDevStatus, currDevStatus);
        if (currentStatusChanged
                && newDevStatus != null
                && !newDevStatus.getName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissionsFactory.get().isUserRoleAdmin()
                && !resourcePermissionsFactory.get().isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin");
            getMessages().add(msg);
            return false;
        }
        return true;
    }
}
