package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;

@Entity
@Table(name = "complaint_surveillance_map")
public class ComplaintSurveillanceMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_surveillance_map_id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "surveillance_id", nullable = false)
    private Long surveillanceId;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Transient
    private SurveillanceBasicEntity surveillance;

    public ComplaintSurveillanceMapEntity() {

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(final Long complaintId) {
        this.complaintId = complaintId;
    }

    public Long getSurveillanceId() {
        return surveillanceId;
    }

    public void setSurveillanceId(final Long surveillanceId) {
        this.surveillanceId = surveillanceId;
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

    public SurveillanceBasicEntity getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(SurveillanceBasicEntity surveillance) {
        this.surveillance = surveillance;
    }

    @Override
    public String toString() {
        return "ComplaintSurveillanceMapEntity [id=" + id + ", complaintId=" + complaintId + ", surveillanceId="
                + surveillanceId + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate
                + ", lastModifiedUser=" + lastModifiedUser + ", deleted=" + deleted + ", surveillance=" + surveillance
                + "]";
    }
}
