package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
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

import gov.healthit.chpl.util.Util;

/**
 * Developer status entity.
 * @author alarned
 *
 */
@Entity
@Table(name = "vendor_status_history")
public class DeveloperStatusEventEntity implements Serializable {
    private static final long serialVersionUID = 1730728043307135377L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vendor_status_history_id", nullable = false)
    private Long id;

    @Column(name = "vendor_id")
    private Long developerId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", insertable = false, updatable = false)
    private DeveloperEntity developer;

    @Column(name = "vendor_status_id")
    private Long developerStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_status_id", insertable = false, updatable = false)
    private DeveloperStatusEntity developerStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "status_date")
    private Date statusDate;
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

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public DeveloperEntity getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperEntity developer) {
        this.developer = developer;
    }

    public Long getDeveloperStatusId() {
        return developerStatusId;
    }

    public void setDeveloperStatusId(final Long developerStatusId) {
        this.developerStatusId = developerStatusId;
    }

    public DeveloperStatusEntity getDeveloperStatus() {
        return developerStatus;
    }

    public void setDeveloperStatus(final DeveloperStatusEntity developerStatus) {
        this.developerStatus = developerStatus;
    }

    public Date getStatusDate() {
        return Util.getNewDate(statusDate);
    }

    public void setStatusDate(final Date statusDate) {
        this.statusDate = Util.getNewDate(statusDate);
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

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Developer Status Event Entity: ["
                + "[Developer: " + this.developer.getName() + "] "
                + "[Status Date: " + this.statusDate.toString() + "] "
                + "[Status: " + this.developerStatus.getName() + "] "
                + "[Reason: " + this.reason + "]"
                + "]";
    }
}
