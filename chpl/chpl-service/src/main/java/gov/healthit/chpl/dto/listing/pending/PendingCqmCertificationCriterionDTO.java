package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCertificationCriteriaEntity;

public class PendingCqmCertificationCriterionDTO implements Serializable {
    private static final long serialVersionUID = -7807505277545563984L;
    private Long id;
    private Long cqmId;
    private Long certificationId;
    private String certificationCriteriaNumber;
    private CertificationCriterionDTO criterion;

    public PendingCqmCertificationCriterionDTO() {
    }

    public PendingCqmCertificationCriterionDTO(PendingCqmCertificationCriteriaEntity entity) {
        this.id = entity.getId();
        this.cqmId = entity.getPendingCqmId();
        this.certificationId = entity.getCertificationId();
        this.criterion = new CertificationCriterionDTO(entity.getCertificationCriteria());
        if (entity.getCertificationCriteria() != null) {
            this.certificationCriteriaNumber = entity.getCertificationCriteria().getNumber();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCqmId() {
        return cqmId;
    }

    public void setCqmId(final Long cqmId) {
        this.cqmId = cqmId;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(final Long certificationId) {
        this.certificationId = certificationId;
    }

    public String getCertificationCriteriaNumber() {
        return certificationCriteriaNumber;
    }

    public void setCertificationCriteriaNumber(final String certificationCriteriaNumber) {
        this.certificationCriteriaNumber = certificationCriteriaNumber;
    }

    public CertificationCriterionDTO getCriterion() {
        return criterion;
    }

    public void setCriterion(CertificationCriterionDTO criterion) {
        this.criterion = criterion;
    }

}
