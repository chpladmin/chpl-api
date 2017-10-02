package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.PendingCqmCertificationCriteriaEntity;

public class PendingCqmCertificationCriterionDTO implements Serializable {
    private static final long serialVersionUID = -7807505277545563984L;
    private Long id;
    private Long cqmId;
    private Long certificationId;
    private String certificationCriteriaNumber;

    public PendingCqmCertificationCriterionDTO() {
    }

    public PendingCqmCertificationCriterionDTO(PendingCqmCertificationCriteriaEntity entity) {
        this.id = entity.getId();
        this.cqmId = entity.getPendingCqmId();
        this.certificationId = entity.getCertificationId();
        if (entity.getCertificationCriteria() != null) {
            this.certificationCriteriaNumber = entity.getCertificationCriteria().getNumber();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCqmId() {
        return cqmId;
    }

    public void setCqmId(Long cqmId) {
        this.cqmId = cqmId;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(Long certificationId) {
        this.certificationId = certificationId;
    }

    public String getCertificationCriteriaNumber() {
        return certificationCriteriaNumber;
    }

    public void setCertificationCriteriaNumber(String certificationCriteriaNumber) {
        this.certificationCriteriaNumber = certificationCriteriaNumber;
    }

}
