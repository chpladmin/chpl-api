package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperWebsiteWellFormedValidation extends ValidationRule<DeveloperValidationContext> {
    private UrlValidator urlValidator;

    public DeveloperWebsiteWellFormedValidation() {
        this.urlValidator = new UrlValidator();
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (!StringUtils.isEmpty(context.getDeveloperDTO().getWebsite())
                && !urlValidator.isValid(context.getDeveloperDTO().getWebsite())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.websiteIsInvalid"));
            return false;
        }
        return true;
    }
}
