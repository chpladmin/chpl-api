package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductUploadActionPermissions")
public class UploadActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

}
