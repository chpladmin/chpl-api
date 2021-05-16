package gov.healthit.chpl.domain.complaint;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintCriterionMap {
    private Long id;
    private Long complaintId;
    private Long certificationCriterionId;
    private CertificationCriterion certificationCriterion;
}
