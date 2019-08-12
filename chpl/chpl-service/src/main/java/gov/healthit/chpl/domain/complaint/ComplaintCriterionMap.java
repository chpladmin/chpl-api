package gov.healthit.chpl.domain.complaint;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.ComplaintCriterionMapDTO;

public class ComplaintCriterionMap {
    private Long id;
    private Long complaintId;
    private Long certificationCriterionId;
    private CertificationCriterion certificationCriterion;

    public ComplaintCriterionMap() {

    }

    public ComplaintCriterionMap(ComplaintCriterionMapDTO dto) {
        BeanUtils.copyProperties(dto, this);
        certificationCriterion = new CertificationCriterion(dto.getCertificationCriterion());
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

    public CertificationCriterion getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(CertificationCriterion certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }

}
