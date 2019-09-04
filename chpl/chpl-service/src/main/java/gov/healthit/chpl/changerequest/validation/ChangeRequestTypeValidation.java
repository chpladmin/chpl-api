package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestTypeValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Is it null?
        if (context.getChangeRequest().getChangeRequestType() == null
                || context.getChangeRequest().getChangeRequestType().getId() == null) {
            getMessages().add(getErrorMessage("changeRequest.changeRequestType.required"));
            return false;
        }

        // Does it exist in the DB?
        try {
            context.getChangeRequestTypeDAO()
                    .getChangeRequestTypeById(context.getChangeRequest().getChangeRequestType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.changeRequestType.invalid"));
            return false;
        }

        return true;
    }

}
