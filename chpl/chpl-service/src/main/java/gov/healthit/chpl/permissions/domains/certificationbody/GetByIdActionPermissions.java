package gov.healthit.chpl.permissions.domains.certificationbody;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationBodyGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertificationBodyDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleUserCreator()) {
            return true;
        } else {
            CertificationBodyDTO dto = (CertificationBodyDTO) obj;
            return isAcbValidForCurrentUser(dto.getId());
        }
    }
}
