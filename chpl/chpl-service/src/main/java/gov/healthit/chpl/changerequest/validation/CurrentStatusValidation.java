package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class CurrentStatusValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // It's fine if it's not set... We aren't going to do anything with it.
        if (context.getChangeRequest().getCurrentStatus() == null
                || context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType() == null
                || context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId() == null) {
            return true;
        }

        // Make sure the current status type is is valid
        // Does it exist in the DB?
        try {
            context.getChangeRequestStatusTypeDAO().getChangeRequestStatusTypeById(
                    context.getChangeRequest().getCurrentStatus().getChangeRequestStatusType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.statusType.invalid"));
            return false;
        }

        return true;
    }

}
