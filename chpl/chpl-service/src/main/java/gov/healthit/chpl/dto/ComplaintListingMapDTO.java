package gov.healthit.chpl.dto;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.ComplaintListingMap;
import gov.healthit.chpl.entity.ComplaintListingMapEntity;

public class ComplaintListingMapDTO {
    private Long id;
    private Long complaintId;
    private Long certifiedProductId;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private String chplProductNumber;

    public ComplaintListingMapDTO() {

    }

    public ComplaintListingMapDTO(ComplaintListingMapEntity entity) {
        BeanUtils.copyProperties(entity, this);
    }

    public ComplaintListingMapDTO(ComplaintListingMap domain) {
        BeanUtils.copyProperties(domain, this);
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

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
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

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

}
