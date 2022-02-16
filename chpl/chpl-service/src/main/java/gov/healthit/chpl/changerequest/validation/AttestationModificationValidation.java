package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class AttestationModificationValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (isCurrentStatusEqual(context) && !changeRequestDetailsEquals(context)) {
            getMessages().add(getErrorMessage("changeRequest.noChangesAllowed"));
            return false;
        } else {
            return true;
        }
    }

    private Boolean isCurrentStatusEqual(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId().equals(
                context.getNewChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
    }

    private Boolean changeRequestDetailsEquals(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getDetails() instanceof ChangeRequestAttestationSubmission) {
            return ChangeRequestAttestationSubmission.cast(context.getNewChangeRequest().getDetails()).matches(
                    ChangeRequestAttestationSubmission.cast(context.getOrigChangeRequest().getDetails()));
        } else {
            return false;
        }
    }
}
