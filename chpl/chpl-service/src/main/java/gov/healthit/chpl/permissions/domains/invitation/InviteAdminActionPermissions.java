package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("invitationInviteAdminActionPermissions")
public class InviteAdminActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return Util.isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
