package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("invitationInviteOncStaffActionPermissions")
public class InviteOncStaffActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
