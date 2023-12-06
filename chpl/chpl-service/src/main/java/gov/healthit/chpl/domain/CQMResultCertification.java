package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CQMResultCertification implements Serializable {
    private static final long serialVersionUID = 2547864525772721622L;

    @Schema(description = "CQM to criteria mapping internal ID")
    private Long id;

    @Schema(description = "Criteria internal ID")
    private Long certificationId;

    @Schema(description = "Certification number (i.e. 170.314 (c)(1)) of the criteria")
    private String certificationNumber;

    private CertificationCriterion criterion;

    public CQMResultCertification(CQMResultCriteriaDTO dto) {
        this.id = dto.getId();
        this.certificationId = dto.getCriterionId();
        this.criterion = dto.getCriterion();
        if (dto.getCriterion() != null) {
            this.certificationNumber = dto.getCriterion().getNumber();
        }
    }
}
