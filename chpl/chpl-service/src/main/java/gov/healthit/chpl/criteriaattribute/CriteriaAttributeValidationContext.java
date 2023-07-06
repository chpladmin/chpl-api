package gov.healthit.chpl.criteriaattribute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaAttributeValidationContext {
    private CriteriaAttributeDAO criteriaAttributeDAO;
    private CriteriaAttribute criteriaAttribe;
    private String name;

    @Builder.Default
    private Boolean isRegulatoryTextCitationRequired = false;

    @Builder.Default
    private Boolean startDayRequired = false;

    @Builder.Default
    private Boolean endDayRequired = false;

    @Builder.Default
    private Boolean requiredDayRequired = false;

    @Builder.Default
    private Boolean ruleRequired = false;
}
