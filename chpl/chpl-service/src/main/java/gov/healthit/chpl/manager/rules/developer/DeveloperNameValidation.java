package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperNameValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (ObjectUtils.isEmpty(context.getDeveloper().getName())) {
            getMessages().add(getErrorMessage("developer.nameRequired"));
            return false;
        }
        return true;
    }
}
