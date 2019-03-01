package gov.healthit.chpl.permissions.domains.certificationbody;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationBodyUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertificationBodyDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            CertificationBodyDTO dto = (CertificationBodyDTO) obj;
            return isAcbValidForCurrentUser(dto.getId());
        } else {
            return false;
        }
    }
}
