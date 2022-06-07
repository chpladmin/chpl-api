package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.ValidationUtils;

public class WebsiteValidation extends ValidationRule<ChangeRequestValidationContext> {
    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
            boolean websiteComponentsValid = true;
            if (!isWebsitePopulated(details.getWebsite())) {
                getMessages().add(getErrorMessage("developer.websiteRequired"));
                websiteComponentsValid = false;
            } else if (!isWebsiteFormatValid(context.getValidationUtils(), details.getWebsite())) {
                getMessages().add(getErrorMessage("developer.websiteIsInvalid"));
                websiteComponentsValid = false;
            }
            return websiteComponentsValid;
        }
        return true;
    }
    private boolean isWebsitePopulated(String website) {
        return StringUtils.isNotEmpty(website);
    }

    private boolean isWebsiteFormatValid(ValidationUtils utils, String website) {
        return utils.isWellFormedUrl(website);
    }
}
