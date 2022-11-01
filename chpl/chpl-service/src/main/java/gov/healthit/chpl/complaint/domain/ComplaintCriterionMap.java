package gov.healthit.chpl.complaint.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintCriterionMap implements Serializable {
    private static final long serialVersionUID = -1626200650447432740L;

    private Long id;
    private Long complaintId;
    private Long certificationCriterionId;
    private CertificationCriterion certificationCriterion;
}
