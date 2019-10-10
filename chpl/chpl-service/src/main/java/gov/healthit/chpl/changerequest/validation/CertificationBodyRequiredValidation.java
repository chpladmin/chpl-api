package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class CertificationBodyRequiredValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public CertificationBodyRequiredValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // If the role is ADMIN or ONC, we require an ACB
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            if (ChangeRequestStatusService.doesCurrentStatusExist(context.getChangeRequest())) {
                if (context.getChangeRequest().getCurrentStatus().getCertificationBody() == null
                        || context.getChangeRequest().getCurrentStatus().getCertificationBody().getId() == null) {
                    getMessages().add(getErrorMessage("changeRequest.missingAcb"));
                    return false;
                }
            }
        }
        return true;
    }

}
