package gov.healthit.chpl.entity.auth;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "invited_user")
public class InvitationEntity {

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invited_user_id", nullable = false)
    private Long id;

    @Column(name = "email", unique = true)
    private String emailAddress;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Column(name = "permission_object_id")
    private Long permissionObjectId;

    @Column(name = "invite_token", unique = true)
    private String inviteToken;

    @Column(name = "confirm_token", unique = true)
    private String confirmToken;

    @Column(name = "created_user_id", unique = true)
    private Long createdUserId;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_permission_id", insertable = false, updatable = false)
    private UserPermissionEntity permission;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Long getUserPermissionId() {
        return userPermissionId;
    }

    public void setUserPermissionId(final Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }

    public Long getPermissionObjectId() {
        return permissionObjectId;
    }

    public void setPermissionObjectId(final Long permissionObjectId) {
        this.permissionObjectId = permissionObjectId;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(final String inviteToken) {
        this.inviteToken = inviteToken;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UserPermissionEntity getPermission() {
        return permission;
    }

    public void setPermission(final UserPermissionEntity permission) {
        this.permission = permission;
    }
}
