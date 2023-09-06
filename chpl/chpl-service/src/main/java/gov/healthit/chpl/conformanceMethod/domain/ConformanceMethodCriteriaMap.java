package gov.healthit.chpl.conformanceMethod.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodCriteriaMapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConformanceMethodCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private ConformanceMethod conformanceMethod;

    public ConformanceMethodCriteriaMap(ConformanceMethodCriteriaMapEntity entity) {
        this.id = entity.getId();
        if (entity.getConformanceMethod() != null) {
            this.conformanceMethod = entity.getConformanceMethod().toDomain();
        }
        if (entity.getCertificationCriterion() != null) {
            this.criterion = entity.getCertificationCriterion().toDomain();
        }
    }
}
