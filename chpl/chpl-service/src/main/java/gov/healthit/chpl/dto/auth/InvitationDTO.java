package gov.healthit.chpl.dto.auth;

import java.util.Date;

import gov.healthit.chpl.auth.entity.InvitationEntity;

public class InvitationDTO {
    private Long id;
    private String email;
    private String inviteToken;
    private String confirmToken;
    private Long createdUserId;
    private boolean deleted;
    private Date creationDate;
    private Long lastModifiedUserId;
    private Date lastModifiedDate;
    private UserPermissionDTO permission;
    //if permission is ROLE_ACB then permissionObjectId is an ACB ID
    //if permission is ROLE_ATL then permissionObjectId is an ATL ID
    //otherwise it may be null
    private Long permissionObjectId;

    public InvitationDTO() {
    }

    public InvitationDTO(InvitationEntity entity) {
        this.id = entity.getId();
        this.email = entity.getEmailAddress();
        this.permissionObjectId = entity.getPermissionObjectId();
        if (entity.getPermission() != null) {
            this.permission = new UserPermissionDTO(entity.getPermission());
        }
        this.inviteToken = entity.getInviteToken();
        this.confirmToken = entity.getConfirmToken();
        this.createdUserId = entity.getCreatedUserId();
        this.deleted = entity.getDeleted();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedUserId = entity.getLastModifiedUser();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }

    public boolean isOlderThan(final long numDaysInMillis) {
        if (this.creationDate == null || this.lastModifiedDate == null) {
            return true;
        }

        Date now = new Date();
        if ((now.getTime() - this.lastModifiedDate.getTime()) > numDaysInMillis) {
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(final String token) {
        this.inviteToken = token;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getPermissionObjectId() {
        return permissionObjectId;
    }

    public void setPermissionObjectId(final Long permissionObjectId) {
        this.permissionObjectId = permissionObjectId;
    }

    public UserPermissionDTO getPermission() {
        return permission;
    }

    public void setPermission(final UserPermissionDTO permission) {
        this.permission = permission;
    }

    public Long getLastModifiedUserId() {
        return lastModifiedUserId;
    }

    public void setLastModifiedUserId(final Long lastModifiedUserId) {
        this.lastModifiedUserId = lastModifiedUserId;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(final String confirmToken) {
        this.confirmToken = confirmToken;
    }

    public Long getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(final Long createdUserId) {
        this.createdUserId = createdUserId;
    }
}
