package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("securedUserGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {
    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Override
    public boolean hasAccess() {
        return false;
    }

    // Original security:
    // @PostFilter("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or
    // hasPermission(filterObject, 'read') or hasPermission(filterObject,
    // admin)")
    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof UserDTO)) {
            return false;
        } else {
            return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                    || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), (UserDTO) obj,
                            BasePermission.ADMINISTRATION);
        }
    }
}
