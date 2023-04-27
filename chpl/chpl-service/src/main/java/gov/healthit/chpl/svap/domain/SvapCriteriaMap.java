package gov.healthit.chpl.svap.domain;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.svap.entity.SvapCriteriaMapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SvapCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private Svap svap;

    public SvapCriteriaMap(SvapCriteriaMapEntity entity) {
        this.id = entity.getId();
        if (entity.getSvap() != null) {
            this.svap = new Svap(entity.getSvap());
        }
        if (entity.getCriteria() != null) {
            this.criterion = entity.getCriteria().toDomain();
        }
    }
}
