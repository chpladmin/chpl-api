package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsAddDeleteActionPermissions")
public class DeleteAcbActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertificationBodyDTO)) {
            return false;
        } else if (Util.isUserRoleAdmin() || Util.isUserRoleOnc()) {
            return true;
        } else if (Util.isUserRoleAcbAdmin()) {
            CertificationBodyDTO acb = (CertificationBodyDTO) obj;
            return isAcbValidForCurrentUser(acb.getId());
        } else {
            return false;
        }
    }

}
