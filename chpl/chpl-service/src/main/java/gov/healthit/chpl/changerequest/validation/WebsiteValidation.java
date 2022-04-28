package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class WebsiteValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            if (isChangeRequestWebsiteValid((ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails())) {
                if (!context.getValidationUtils().isWellFormedUrl(((ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails()).getWebsite())) {
                    getMessages().add(getErrorMessage("changeRequest.details.website.invalidFormat"));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isChangeRequestWebsiteValid(ChangeRequestDeveloperDemographics details) {
        return StringUtils.isNotEmpty(details.getWebsite());
    }
}
