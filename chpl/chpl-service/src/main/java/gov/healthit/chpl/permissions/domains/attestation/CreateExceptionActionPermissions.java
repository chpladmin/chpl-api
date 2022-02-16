package gov.healthit.chpl.permissions.domains.attestation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("attestationCreateExceptionActionPermissions")
public class CreateExceptionActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof AttestationPeriodDeveloperException)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AttestationPeriodDeveloperException apde = (AttestationPeriodDeveloperException) obj;
            return isCurrentAcbUserAssociatedWithDeveloper(apde.getDeveloper().getDeveloperId());
        } else {
            return false;
        }
    }
}
