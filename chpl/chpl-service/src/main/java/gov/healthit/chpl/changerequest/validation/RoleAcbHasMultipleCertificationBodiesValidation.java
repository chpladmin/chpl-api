package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class RoleAcbHasMultipleCertificationBodiesValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleAcbAdmin()
                && context.getResourcePermissions().getAllAcbsForCurrentUser().size() > 1) {
            getMessages().add(getErrorMessage("changeRequest.multipleAcbs"));
            return false;
        }
        return true;
    }

}
