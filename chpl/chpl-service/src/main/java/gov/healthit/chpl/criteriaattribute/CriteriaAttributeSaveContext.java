package gov.healthit.chpl.criteriaattribute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaAttributeSaveContext {
    private CriteriaAttributeDAO criteriaAttributeDAO;
    private CriteriaAttribute criteriaAttribute;
    private String name;
}
