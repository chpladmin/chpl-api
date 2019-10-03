package gov.healthit.chpl.manager.rules.developer;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ValidationUtils;

public class DeveloperWebsiteWellFormedValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (!StringUtils.isEmpty(context.getDeveloperDTO().getWebsite())
                && !ValidationUtils.isWellFormedUrl(context.getDeveloperDTO().getWebsite())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.websiteIsInvalid"));
            return false;
        }
        return true;
    }
}
