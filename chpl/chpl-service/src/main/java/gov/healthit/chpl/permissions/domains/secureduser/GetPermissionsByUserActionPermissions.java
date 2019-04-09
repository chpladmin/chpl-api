package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("securedUserGetPermissionsByUserActionPermissions")
public class GetPermissionsByUserActionPermissions extends ActionPermissions {

    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasAccess() {
        return false;
    }

    // Original Security:
    // @PreAuthorize("hasAnyRole('ROLE_USER_AUTHENTICATOR', 'ROLE_ADMIN',
    // 'ROLE_ONC', 'ROLE_ACB', 'ROLE_ATL') or hasPermission(#user, 'read') or
    // hasPermission(#user, admin)")
    @Override
    public boolean hasAccess(Object obj) {
        return getResourcePermissions().isUserRoleUserAuthenticator() || getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleAtlAdmin()
                || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), (UserDTO) obj,
                        BasePermission.ADMINISTRATION)
                || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), (UserDTO) obj, BasePermission.READ);
    }

}
