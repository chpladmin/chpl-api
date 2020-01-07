package gov.healthit.chpl.permissions.domains.certificationresults;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationResultsCreatePermissions")
public class CreatePermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof CertifiedProductSearchDetails)) {
            return false;
        } else {
            return getResourcePermissions().isUserRoleAdmin();
        }
    }
}
