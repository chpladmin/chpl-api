package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetDetailsByIdActionPermissions")
public class GetDetailsByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
