package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.auth.InvitationDTO;
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
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof UserInvitationDTO)) {
            return false;
        } else {
            UserInvitationDTO userInvitation = (UserInvitationDTO) obj;
            UserDTO userToUpdate = userInvitation.getUser();
            InvitationDTO invitation = userInvitation.getInvitation();
            if (userToUpdate == null || userToUpdate.getPermission() == null
                    || invitation == null || invitation.getPermission() == null) {
                return false;
            }
            //if user is an acb they cannot receive permissions on anything besides another acb
            //if user is an atl they cannot receive permissions on anything besides another atl
            //no other types of user accounts should be able to have permissions updated
            if (userToUpdate.getPermission().getAuthority().equals(Authority.ROLE_ACB)
                    && invitation.getPermission().getAuthority().equals(Authority.ROLE_ACB)) {
                return true;
            } else if (userToUpdate.getPermission().getAuthority().equals(Authority.ROLE_ATL)
                    && invitation.getPermission().getAuthority().equals(Authority.ROLE_ATL)) {
                return true;
            }
            return false;
        }
    }

}
