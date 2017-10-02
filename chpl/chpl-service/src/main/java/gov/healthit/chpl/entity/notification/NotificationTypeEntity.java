package gov.healthit.chpl.entity.notification;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

@Entity
@Immutable
@Table(name = "notification_type")
public class NotificationTypeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "requires_acb")
    private Boolean requiresAcb;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "notificationTypeId")
    @Basic(optional = false)
    @Column(name = "notification_type_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<NotificationPermissionEntity> permissions = new HashSet<NotificationPermissionEntity>();

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

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<NotificationPermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<NotificationPermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public Boolean getRequiresAcb() {
        return requiresAcb;
    }

    public void setRequiresAcb(Boolean requiresAcb) {
        this.requiresAcb = requiresAcb;
    }

}
