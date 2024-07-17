package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperEditStatusHistoryValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissionsFactory resourcePermissionsFactory;

    public DeveloperEditStatusHistoryValidation(ResourcePermissionsFactory resourcePermissionsFactory) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    /**
     * If the status history has been modified, the user must be role admin
     * except that an acb admin can change to UnderCertificationBanByOnc
     * triggered by listing status update
     */
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        Developer updatedDev = context.getDeveloper();
        Developer beforeDev = context.getBeforeDev();
        DeveloperStatus newDevStatus = updatedDev.getCurrentStatusEvent() != null ? updatedDev.getCurrentStatusEvent().getStatus() : null;

        boolean devStatusHistoryUpdated = isStatusHistoryUpdated(beforeDev, updatedDev);

        if (devStatusHistoryUpdated && newDevStatus != null
                && newDevStatus.getName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissionsFactory.get().isUserRoleAdmin()
                && !resourcePermissionsFactory.get().isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin",
                    DeveloperStatusType.UnderCertificationBanByOnc.toString());
            getMessages().add(msg);
            return false;
        } else if (devStatusHistoryUpdated && newDevStatus != null
                && !newDevStatus.getName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissionsFactory.get().isUserRoleAdmin()
                && !resourcePermissionsFactory.get().isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusHistoryChangeNotAllowedWithoutAdmin");
            getMessages().add(msg);
            return false;
        }
        return true;
    }

    private static boolean isStatusHistoryUpdated(Developer original, Developer changed) {
        boolean hasChanged = false;
        if ((original.getStatuses() != null && changed.getStatuses() == null)
                || (original.getStatuses() == null && changed.getStatuses() != null)
                || (original.getStatuses().size() != changed.getStatuses().size())) {
            hasChanged = true;
        } else {
            for (DeveloperStatusEvent origStatusEvent : original.getStatuses()) {
                boolean foundMatchInChanged = false;
                for (DeveloperStatusEvent changedStatusEvent : changed.getStatuses()) {
                    if (origStatusEvent.equals(changedStatusEvent)) {
                        foundMatchInChanged = true;
                    }
                }
                hasChanged = hasChanged || !foundMatchInChanged;
            }
        }
        return hasChanged;
    }
}
