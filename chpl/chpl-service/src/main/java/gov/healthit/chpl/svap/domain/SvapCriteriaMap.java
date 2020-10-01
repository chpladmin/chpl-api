package gov.healthit.chpl.svap.domain;

import java.util.Objects;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
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
        if (Objects.nonNull(entity.getSvap())) {
            this.svap = new Svap(entity.getSvap());
        }
        if (Objects.nonNull(entity.getCriteria())) {
            this.criterion = new CertificationCriterion(new CertificationCriterionDTO(entity.getCriteria()));
        }
    }
}
