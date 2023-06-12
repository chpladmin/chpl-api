package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("invitationUpdateFromInvitationActionPermissions")
public class UpdateFromInvitationActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    /**
     * Checks whether a user account can be updated
     * with the permissions on the given invitation.
     */
    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof UserInvitationDTO)) {
            return false;
        } else {
            UserInvitationDTO userInvitation = (UserInvitationDTO) obj;
            UserDTO userToUpdate = userInvitation.getUser();
            UserInvitation invitation = userInvitation.getInvitation();
            if (userToUpdate == null || userToUpdate.getPermission() == null
                    || invitation == null || invitation.getRole() == null) {
                return false;
            }
            //if user is an acb they cannot receive permissions on anything besides another acb
            //if user is an atl they cannot receive permissions on anything besides another atl
            //if a user is a developer they cannot receive permissions on anything besides another developers
            //no other types of user accounts should be able to have permissions updated
            if (userToUpdate.getPermission().getAuthority().equals(Authority.ROLE_ACB)
                    && invitation.getRole().equals(Authority.ROLE_ACB)) {
                return true;
            } else if (userToUpdate.getPermission().getAuthority().equals(Authority.ROLE_DEVELOPER)
                    && invitation.getRole().equals(Authority.ROLE_DEVELOPER)) {
                return true;
            }
            return false;
        }
    }

}
