package gov.healthit.chpl.permissions.domains.surveillance;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceBasicReportActionPermissions")
public class BasicReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAdmin() || Util.isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
