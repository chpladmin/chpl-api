package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class SelfDeveloperValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
            if (details.getSelfDeveloper() == null) {
                getMessages().add(getErrorMessage("changeRequest.demographics.selfDeveloper.invalid"));
                return false;
            }
        }
        return true;
    }
}
