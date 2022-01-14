package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> getErrorMessages(ChangeRequestValidationContext object) {
        List<String> errorMessages = new ArrayList<String>();

        if (resourcePermissions.isUserRoleAcbAdmin()
                && resourcePermissions.getAllAcbsForCurrentUser().size() > 1) {
            errorMessages.add(getErrorMessage("changeRequest.multipleAcbs"));
        }
        return errorMessages;
    }

}
