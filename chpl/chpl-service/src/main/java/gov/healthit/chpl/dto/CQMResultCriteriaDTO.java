package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CQMResultCriteriaEntity;

public class CQMResultCriteriaDTO implements Serializable {
    private static final long serialVersionUID = 8485439145849298419L;
    private Long id;
    private Long cqmResultId;
    private Long criterionId;
    private CertificationCriterionDTO criterion;

    public CQMResultCriteriaDTO() {
    }

    public CQMResultCriteriaDTO(CQMResultCriteriaEntity entity) {

        this.id = entity.getId();
        this.cqmResultId = entity.getCqmResultId();
        this.criterionId = entity.getCertificationCriterionId();
        if (entity.getCertCriteria() != null) {
            this.criterion = new CertificationCriterionDTO(entity.getCertCriteria());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCqmResultId() {
        return cqmResultId;
    }

    public void setCqmResultId(final Long cqmResultId) {
        this.cqmResultId = cqmResultId;
    }

    public Long getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(final Long criterionId) {
        this.criterionId = criterionId;
    }

    public CertificationCriterionDTO getCriterion() {
        return criterion;
    }

    public void setCriterion(final CertificationCriterionDTO criterion) {
        this.criterion = criterion;
    }
}
