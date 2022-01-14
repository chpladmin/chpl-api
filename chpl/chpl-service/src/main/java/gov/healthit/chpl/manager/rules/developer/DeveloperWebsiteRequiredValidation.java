package gov.healthit.chpl.manager.rules.developer;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperWebsiteRequiredValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean getErrorMessages(DeveloperValidationContext context) {
        if (StringUtils.isEmpty(context.getDeveloperDTO().getWebsite())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.websiteRequired"));
            return false;
        }
        return true;
    }
}
