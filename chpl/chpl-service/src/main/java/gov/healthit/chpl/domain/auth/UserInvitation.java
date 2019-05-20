package gov.healthit.chpl.domain.auth;

import gov.healthit.chpl.dto.auth.InvitationDTO;

public class UserInvitation {

    private String emailAddress;
    private String role;
    private Long permissionObjectId;
    private String hash;

    public UserInvitation() {}

    public UserInvitation(final InvitationDTO dto) {
        this.emailAddress = dto.getEmail();
        this.permissionObjectId = dto.getPermissionObjectId();
        this.hash = dto.getInviteToken();
        if (dto.getPermission() != null) {
            this.role = dto.getPermission().getAuthority();
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public Long getPermissionObjectId() {
        return permissionObjectId;
    }

    public void setPermissionObjectId(final Long permissionObjectId) {
        this.permissionObjectId = permissionObjectId;
    }

}
