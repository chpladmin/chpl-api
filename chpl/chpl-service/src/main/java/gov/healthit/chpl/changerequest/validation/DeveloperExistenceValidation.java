package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperExistenceValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Is the developer null?
        if (context.getNewChangeRequest().getDeveloper() == null
                || context.getNewChangeRequest().getDeveloper().getDeveloperId() == null) {
            getMessages().add(getErrorMessage("changeRequest.developer.required"));
            return false;
        }

        try {
            context.getValidationDAOs().getDeveloperDAO().getById(context.getNewChangeRequest().getDeveloper().getDeveloperId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.developer.invalid"));
            return false;
        } catch (Exception e) {
            // This would probably be a NPE, but will be caught by other
            // validators
            return true;
        }
        return true;
    }

}
