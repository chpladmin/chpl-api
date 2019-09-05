package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestExistanceValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Is it null?
        if (context.getChangeRequest() == null
                || context.getChangeRequest().getId() == null) {
            getMessages().add(getErrorMessage("changeRequest.changeRequest.required"));
            return false;
        }

        // Does it exist in the DB?
        try {
            context.getChangeRequestDAO().get(context.getChangeRequest().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.invalid"));
            return false;
        }

        return true;
    }

}
