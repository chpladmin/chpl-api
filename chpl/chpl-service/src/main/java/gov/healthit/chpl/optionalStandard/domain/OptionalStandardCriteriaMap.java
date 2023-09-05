package gov.healthit.chpl.optionalStandard.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardCriteriaMapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionalStandardCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private OptionalStandard optionalStandard;

    public OptionalStandardCriteriaMap(OptionalStandardCriteriaMapEntity entity) {
        this.id = entity.getId();
        if (entity.getOptionalStandard() != null) {
            this.optionalStandard = new OptionalStandard(entity.getOptionalStandard());
        }
        if (entity.getCriteria() != null) {
            this.criterion = entity.getCriteria().toDomain();
        }
    }
}
