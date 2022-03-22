package gov.healthit.chpl.permissions.domains.attestation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("attestationCreateExceptionActionPermissions")
public class CreateExceptionActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long developerId = (Long) obj;
            return isCurrentAcbUserAssociatedWithDeveloper(developerId);
        } else {
            return false;
        }
    }
}
