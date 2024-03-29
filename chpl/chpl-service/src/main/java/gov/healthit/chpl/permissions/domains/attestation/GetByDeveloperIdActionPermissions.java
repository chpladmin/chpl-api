package gov.healthit.chpl.permissions.domains.attestation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("attestationGetByDeveloperIdActionPermissions")
public class GetByDeveloperIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            Long developerId = (Long) obj;
            return isDeveloperValidForCurrentUser(developerId);
        } else {
            return false;
        }
    }

}
