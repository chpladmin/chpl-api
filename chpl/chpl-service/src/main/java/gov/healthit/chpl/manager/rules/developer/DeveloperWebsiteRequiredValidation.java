package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperWebsiteRequiredValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (ObjectUtils.isEmpty(context.getDeveloper().getWebsite())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.websiteRequired"));
            return false;
        }
        return true;
    }
}
