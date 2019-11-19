package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperEditStatusHistoryValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissions resourcePermissions;

    public DeveloperEditStatusHistoryValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    /**
     * If the status history has been modified, the user must be role admin
     * except that an acb admin can change to UnderCertificationBanByOnc
     * triggered by listing status update
     */
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO updatedDev = context.getDeveloperDTO();
        DeveloperDTO beforeDev = context.getBeforeDev();
        DeveloperStatusEventDTO newDevStatus = updatedDev.getStatus();

        boolean devStatusHistoryUpdated = isStatusHistoryUpdated(beforeDev, updatedDev);

        if (devStatusHistoryUpdated
                && newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin",
                    DeveloperStatusType.UnderCertificationBanByOnc.toString());
            getMessages().add(msg);
            return false;
        } else if (devStatusHistoryUpdated
                && !newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && resourcePermissions.isUserRoleAdmin() && resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusHistoryChangeNotAllowedWithoutAdmin");
            getMessages().add(msg);
            return false;
        }
        return true;
    }

    private static boolean isStatusHistoryUpdated(final DeveloperDTO original, final DeveloperDTO changed) {
        boolean hasChanged = false;
        if ((original.getStatusEvents() != null && changed.getStatusEvents() == null)
                || (original.getStatusEvents() == null && changed.getStatusEvents() != null)
                || (original.getStatusEvents().size() != changed.getStatusEvents().size())) {
            hasChanged = true;
        } else {
            // neither status history is null and they have the same size
            // history arrays so now check for any differences in the values of
            // each
            for (DeveloperStatusEventDTO origStatusHistory : original.getStatusEvents()) {
                boolean foundMatchInChanged = false;
                for (DeveloperStatusEventDTO changedStatusHistory : changed.getStatusEvents()) {
                    if (origStatusHistory.getStatus().getId() != null
                            && changedStatusHistory.getStatus().getId() != null
                            && origStatusHistory.getStatus().getId().equals(changedStatusHistory.getStatus().getId())
                            && origStatusHistory.getStatusDate().getTime() == changedStatusHistory.getStatusDate()
                                    .getTime()) {
                        foundMatchInChanged = true;
                    }
                }
                hasChanged = hasChanged || !foundMatchInChanged;
            }
        }
        return hasChanged;
    }
}
