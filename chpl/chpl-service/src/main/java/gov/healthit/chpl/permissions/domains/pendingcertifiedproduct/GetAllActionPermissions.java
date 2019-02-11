package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
