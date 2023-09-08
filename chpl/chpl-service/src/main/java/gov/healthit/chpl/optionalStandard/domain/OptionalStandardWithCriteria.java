package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OptionalStandardWithCriteria implements Serializable {
    private static final long serialVersionUID = 620315627813875501L;
    private Long id;
    private String citation;
    private String description;

    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
}
