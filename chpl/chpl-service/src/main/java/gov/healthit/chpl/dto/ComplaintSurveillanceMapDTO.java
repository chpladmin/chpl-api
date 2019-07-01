package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.entity.ComplaintSurveillanceMapEntity;

public class ComplaintSurveillanceMapDTO implements Serializable {
    private static final long serialVersionUID = -4006510869292715446L;

    private Long id;
    private Long complaintId;
    private Long surveillanceId;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private SurveillanceLiteDTO surveillance;

    public ComplaintSurveillanceMapDTO() {

    }

    public ComplaintSurveillanceMapDTO(ComplaintSurveillanceMapEntity entity) {
        BeanUtils.copyProperties(entity, this);
        this.surveillance = new SurveillanceLiteDTO(entity.getSurveillance());
    }

    public ComplaintSurveillanceMapDTO(ComplaintSurveillanceMap domain) {
        BeanUtils.copyProperties(domain, this);
        this.surveillance = new SurveillanceLiteDTO(domain.getSurveillance());
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

    public SurveillanceLiteDTO getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(SurveillanceLiteDTO surveillance) {
        this.surveillance = surveillance;
    }

    @Override
    public String toString() {
        return "ComplaintSurveillanceMapDTO [id=" + id + ", complaintId=" + complaintId + ", surveillanceId="
                + surveillanceId + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate
                + ", lastModifiedUser=" + lastModifiedUser + ", deleted=" + deleted + ", surveillance=" + surveillance
                + "]";
    }
}
