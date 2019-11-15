package gov.healthit.chpl.changerequest.entity;

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

import gov.healthit.chpl.entity.developer.DeveloperEntity;

@Entity
@Table(name = "change_request")
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_type_id", nullable = false, insertable = true,
            updatable = false)
    private ChangeRequestTypeEntity changeRequestType;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false, insertable = true,
            updatable = false)
    private DeveloperEntity developer;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ChangeRequestTypeEntity getChangeRequestType() {
        return changeRequestType;
    }

    public void setChangeRequestType(final ChangeRequestTypeEntity changeRequestType) {
        this.changeRequestType = changeRequestType;
    }

    public DeveloperEntity getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperEntity developer) {
        this.developer = developer;
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

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "ChangeRequestEntity [id=" + id + ", changeRequestType=" + changeRequestType + ", developer=" + developer
                + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedUser="
                + lastModifiedUser + ", deleted=" + deleted + "]";
    }
}
