package gov.healthit.chpl.dto;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.complaint.ComplaintCriterionMap;
import gov.healthit.chpl.entity.ComplaintCriterionMapEntity;

public class ComplaintCriterionMapDTO {
    private Long id;
    private Long complaintId;
    private Long certificationCriterionId;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private CertificationCriterionDTO certificationCriterion;

    public ComplaintCriterionMapDTO() {

    }

    public ComplaintCriterionMapDTO(ComplaintCriterionMapEntity entity) {
        BeanUtils.copyProperties(entity, this);
        certificationCriterion = new CertificationCriterionDTO(entity.getCertificationCriterion());;
    }

    public ComplaintCriterionMapDTO(ComplaintCriterionMap domain) {
        BeanUtils.copyProperties(domain, this);
        certificationCriterion = new CertificationCriterionDTO(domain.getCertificationCriterion());
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

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
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

    public CertificationCriterionDTO getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(CertificationCriterionDTO certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }
}
