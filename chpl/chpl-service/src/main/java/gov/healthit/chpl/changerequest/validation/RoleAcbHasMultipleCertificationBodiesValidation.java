package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class RoleAcbHasMultipleCertificationBodiesValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public RoleAcbHasMultipleCertificationBodiesValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext object) {
        if (resourcePermissions.isUserRoleAcbAdmin()
                && resourcePermissions.getAllAcbsForCurrentUser().size() > 1) {
            getMessages().add(getErrorMessage("changeRequest.multipleAcbs"));
            return false;
        }
        return true;
    }

}
