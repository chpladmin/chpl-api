package gov.healthit.chpl.attribute;

import java.util.OptionalLong;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeUpToDate {
    private AttributeType attributeType;
    private CertificationCriterion criterion;
    private Boolean upToDate;
    private Boolean eligibleForAttribute;
    private OptionalLong daysUpdatedEarly;

}
