package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("invitationGetActionPermissions")
public class GetActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleInvitedUserCreator();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
