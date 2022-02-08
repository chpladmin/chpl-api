package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestTypeValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getNewChangeRequest().getChangeRequestType() == null
                || context.getNewChangeRequest().getChangeRequestType().getId() == null) {
            getMessages().add(getErrorMessage("changeRequest.changeRequestType.required"));
            return false;
        }

        // Does it exist in the DB?
        try {
            context.getValidationDAOs().getChangeRequestTypeDAO().getChangeRequestTypeById(context.getNewChangeRequest().getChangeRequestType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.changeRequestType.invalid"));
            return false;
        }

        return true;
    }

}
