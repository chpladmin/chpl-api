package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("securedUserGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {
    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasAccess() {
        return false;
    }

    // Original Security:
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB', 'ROLE_ATL',
    // 'ROLE_ONC') or hasPermission(#id, 'gov.healthit.chpl.auth.dto.UserDTO',
    // admin)")
    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else {
            return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                    || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleAtlAdmin()
                    || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), (Long) obj,
                            "gov.healthit.chpl.auth.dto.UserDTO", BasePermission.ADMINISTRATION);
        }

    }

}
