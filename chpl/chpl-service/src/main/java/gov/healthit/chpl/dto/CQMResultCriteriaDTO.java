package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.entity.listing.CQMResultCriteriaEntity;
import lombok.Data;

@Data
public class CQMResultCriteriaDTO implements Serializable {
    private static final long serialVersionUID = 8485439145849298419L;
    private Long id;
    private Long cqmResultId;
    private Long criterionId;
    private CertificationCriterion criterion;

    public CQMResultCriteriaDTO() {
    }

    public CQMResultCriteriaDTO(CQMResultCriteriaEntity entity) {

        this.id = entity.getId();
        this.cqmResultId = entity.getCqmResultId();
        this.criterionId = entity.getCertificationCriterionId();
        if (entity.getCertCriteria() != null) {
            this.criterion = entity.getCertCriteria().toDomain();
        }
    }
}
