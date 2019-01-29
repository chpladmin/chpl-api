package gov.healthit.chpl.permissions.domains.certificationbody;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationBodyAddPermissionActionPermissions")
public class AddPermissionActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertificationBodyDTO)) {
            return false;
        } else if (Util.isUserRoleAdmin() || Util.isUserRoleOnc() || Util.isUserRoleUserCreator()) {
            return true;
        } else {
            CertificationBodyDTO dto = (CertificationBodyDTO) obj;
            return isAcbValidForCurrentUser(dto.getId());
        }
    }
}
