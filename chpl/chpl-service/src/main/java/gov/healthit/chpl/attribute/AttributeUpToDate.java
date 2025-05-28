package gov.healthit.chpl.attribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeUpToDate {
    private CertificationCriterion criterion;
    private Boolean eligibleForAttribute;
    private Boolean expiringButPresent;
    private Boolean requiredButNotPresent;
}
