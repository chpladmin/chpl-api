package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.CertificationStatusEventEntity;
import gov.healthit.chpl.util.Util;

public class CertificationStatusEventDTO implements Serializable {
    private static final long serialVersionUID = 1171613630377844762L;
    private Long id;
    private Long certifiedProductId;
    private Date eventDate;
    private CertificationStatusDTO status;
    private String reason;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CertificationStatusEventDTO() {
    }

    public CertificationStatusEventDTO(CertificationStatusEventEntity entity) {

        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.eventDate = entity.getEventDate();
        if (entity.getCertificationStatus() != null) {
            this.status = new CertificationStatusDTO(entity.getCertificationStatus());
        } else if (entity.getCertificationStatusId() != null) {
            this.status = new CertificationStatusDTO();
            this.status.setId(entity.getCertificationStatusId());
        }
        this.reason = entity.getReason();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Date getEventDate() {
        return Util.getNewDate(eventDate);
    }

    public void setEventDate(final Date eventDate) {
        this.eventDate = Util.getNewDate(eventDate);
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

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public CertificationStatusDTO getStatus() {
        return status;
    }

    public void setStatus(final CertificationStatusDTO status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
