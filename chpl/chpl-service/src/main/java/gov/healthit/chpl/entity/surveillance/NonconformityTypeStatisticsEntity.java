package gov.healthit.chpl.entity.surveillance;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "nonconformity_type_statistics")
public class NonconformityTypeStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "nonconformity_count", nullable = false)
    private Long nonconformityCount;

    @Basic(optional = false)
    @Column(name = "nonconformity_type", nullable = false)
    private String nonconformityType;

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

    public Long getNonconformityCount() {
        return nonconformityCount;
    }

    public void setNonconformityCount(Long nonconformityCount) {
        this.nonconformityCount = nonconformityCount;
    }

    public String getNonconformityType() {
        return nonconformityType;
    }

    public void setNonconformityType(String nonconformityType) {
        this.nonconformityType = nonconformityType;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}
