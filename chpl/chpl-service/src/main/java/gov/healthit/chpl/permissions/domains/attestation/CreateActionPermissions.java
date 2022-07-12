package gov.healthit.chpl.permissions.domains.attestation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("attestationCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof AttestationSubmission)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AttestationSubmission attestationSubmission = (AttestationSubmission) obj;
            return isCurrentAcbUserAssociatedWithDeveloper(attestationSubmission.getDeveloperId());
        } else {
            return false;
        }
    }

}
