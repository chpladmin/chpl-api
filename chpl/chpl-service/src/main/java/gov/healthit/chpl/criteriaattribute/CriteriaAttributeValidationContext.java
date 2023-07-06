package gov.healthit.chpl.criteriaattribute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaAttributeValidationContext {
    private CriteriaAttributeDAO criteriaAttributeDAO;
    private CriteriaAttribute criteriaAttribe;
    private String name;
}
