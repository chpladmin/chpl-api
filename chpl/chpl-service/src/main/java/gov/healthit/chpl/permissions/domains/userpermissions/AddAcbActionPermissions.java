package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsAddAcbActionPermissions")
public class AddAcbActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertificationBody)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleUserCreator()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            CertificationBody acb = (CertificationBody) obj;
            return isAcbValidForCurrentUser(acb.getId());
        } else {
            return false;
        }
    }

}
