package gov.healthit.chpl.manager.rules.developer;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperStatusChangedValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissions resourcePermissions;

    public DeveloperStatusChangedValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    public DeveloperStatusChangedValidation(final DeveloperManager developerManagerImpl,
            final ResourcePermissions resourcePermissions, final DeveloperDAO developerDao) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO updatedDev = context.getDeveloperDTO();
        DeveloperDTO beforeDev = context.getBeforeDev();
        DeveloperStatusEventDTO newDevStatus = updatedDev.getStatus();
        DeveloperStatusEventDTO currDevStatus = beforeDev.getStatus();

        boolean currentStatusChanged = !currDevStatus.getStatus().getStatusName()
                .equals(newDevStatus.getStatus().getStatusName());
        if (currentStatusChanged
                && !newDevStatus.getStatus().getStatusName()
                        .equals(DeveloperStatusType.UnderCertificationBanByOnc.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = msgUtil.getMessage("developer.statusChangeNotAllowedWithoutAdmin");
            getMessages().add(msg);
            return false;
        }
        return true;
    }
}
