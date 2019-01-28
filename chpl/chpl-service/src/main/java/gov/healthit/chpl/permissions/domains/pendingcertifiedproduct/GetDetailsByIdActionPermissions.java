package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetDetailsByIdActionPermissions")
public class GetDetailsByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAdmin() || Util.isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
