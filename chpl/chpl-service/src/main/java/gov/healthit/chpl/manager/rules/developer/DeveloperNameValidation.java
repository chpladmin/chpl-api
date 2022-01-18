package gov.healthit.chpl.manager.rules.developer;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperNameValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean getErrorMessages(DeveloperValidationContext context) {
        if (StringUtils.isEmpty(context.getDeveloperDTO().getName())) {
            getMessages().add(getErrorMessage("developer.nameRequired"));
            return false;
        }
        return true;
    }
}
