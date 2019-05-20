package gov.healthit.chpl.dto.auth;

public class UserInvitationDTO {
    private UserDTO user;
    private InvitationDTO invitation;

    public UserInvitationDTO() {
    }

    public UserInvitationDTO(final UserDTO user, final InvitationDTO invitation) {
        this.user = user;
        this.invitation = invitation;
    }

    public UserDTO getUser() {
        return user;
    }
    public void setUser(final UserDTO user) {
        this.user = user;
    }
    public InvitationDTO getInvitation() {
        return invitation;
    }
    public void setInvitation(final InvitationDTO invitation) {
        this.invitation = invitation;
    }
}
