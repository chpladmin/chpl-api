package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Is the developer null?
        if (context.getChangeRequest().getDeveloper() == null
                || context.getChangeRequest().getDeveloper().getDeveloperId() == null) {
            getMessages().add(getErrorMessage("changeRequest.developer.required"));
            return false;
        }

        // Does it exist in the DB?
        try {
            context.getDeveloperDAO().getById(context.getChangeRequest().getDeveloper().getDeveloperId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.developer.invalid"));
            return false;
        }
        return true;
    }

}
