package gov.healthit.chpl.permissions.domains.certificationbody;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationBodyGetAllUsersActionPermissions")
public class GetAllUsersActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        Long acbId = null;
        if (obj instanceof Long) {
            acbId = (Long) obj;
        } else if (obj instanceof CertificationBody) {
            acbId = ((CertificationBody) obj).getId();
        }
        if (acbId == null) {
            return false;
        }

        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            boolean hasPermissionOnAcb = false;
            for (CertificationBody acb : getResourcePermissions().getAllAcbsForCurrentUser()) {
                if (acb.getId().equals(acbId)) {
                    hasPermissionOnAcb = true;
                }
            }
            return hasPermissionOnAcb;
        }
        return false;
    }

}
