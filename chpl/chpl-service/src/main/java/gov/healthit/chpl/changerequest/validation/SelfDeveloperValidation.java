package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographic;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class SelfDeveloperValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographic details = (ChangeRequestDeveloperDemographic) context.getNewChangeRequest().getDetails();
            if (details.getSelfDeveloper() == null) {
                getMessages().add(getErrorMessage("changeRequest.details.selfDeveloper.invalid"));
                return false;
            }
        }
        return true;
    }
}
