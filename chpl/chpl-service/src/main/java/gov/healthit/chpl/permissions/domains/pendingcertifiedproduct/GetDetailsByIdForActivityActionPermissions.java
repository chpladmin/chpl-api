package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetDetailsByIdForActivityActionPermissions")
public class GetDetailsByIdForActivityActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAdmin() || Util.isUserRoleOnc() || Util.isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
