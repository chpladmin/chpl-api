package gov.healthit.chpl.entity.notification;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.auth.entity.UserPermissionEntity;

@Entity
@Immutable
@Table(name = "notification_type_permission")
public class NotificationPermissionEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "notification_type_id")
    private Long notificationTypeId;

    @Column(name = "permission_id")
    private String permissionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private UserPermissionEntity permission;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNotificationTypeId() {
        return notificationTypeId;
    }

    public void setNotificationTypeId(final Long notificationTypeId) {
        this.notificationTypeId = notificationTypeId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(final String permissionId) {
        this.permissionId = permissionId;
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
