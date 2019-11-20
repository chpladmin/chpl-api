package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang.StringUtils;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.impl.DeveloperManagerImpl;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class DeveloperStatusChangedValidation extends ValidationRule<DeveloperValidationContext> {
    private ResourcePermissions resourcePermissions;

    public DeveloperStatusChangedValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    public DeveloperStatusChangedValidation(final DeveloperManagerImpl developerManagerImpl,
            final ResourcePermissions resourcePermissions, final DeveloperDAO developerDao) {
        this.resourcePermissions = resourcePermissions;
    }

    /**
     * determine if the status has been changed in most cases only allowed by
     * ROLE_ADMIN but ROLE_ACB can change it to UnderCertificationBanByOnc
     */
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
        // Check to see that the Developer's website is valid.
        if (!StringUtils.isEmpty(updatedDev.getWebsite())) {
            if (!ValidationUtils.isWellFormedUrl(updatedDev.getWebsite())) {
                String msg = msgUtil.getMessage("developer.websiteIsInvalid");
                getMessages().add(msg);
                return false;
            }
        }
        return true;
    }
}
