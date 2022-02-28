package gov.healthit.chpl.manager.rules.developer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.validator.routines.UrlValidator;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperWebsiteWellFormedValidation extends ValidationRule<DeveloperValidationContext> {
    private UrlValidator urlValidator;

    public DeveloperWebsiteWellFormedValidation() {
        this.urlValidator = new UrlValidator();
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        if (!ObjectUtils.isEmpty(context.getDeveloper().getWebsite())
                && !urlValidator.isValid(context.getDeveloper().getWebsite())) {
            getMessages().add(context.getErrorMessageUtil().getMessage("developer.websiteIsInvalid"));
            return false;
        }
        return true;
    }
}
