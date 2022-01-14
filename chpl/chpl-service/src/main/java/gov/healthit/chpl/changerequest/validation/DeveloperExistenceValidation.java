package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperExistenceValidation extends ValidationRule<ChangeRequestValidationContext> {

    private DeveloperDAO developerDAO;

    @Autowired
    public DeveloperExistenceValidation(final DeveloperDAO developerDAO) {
        this.developerDAO = developerDAO;
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        // Is the developer null?
        if (context.getNewChangeRequest().getDeveloper() == null
                || context.getNewChangeRequest().getDeveloper().getDeveloperId() == null) {
            errorMessages.add(getErrorMessageFromResource("changeRequest.developer.required"));
            return errorMessages;
        }

        try {
            developerDAO.getById(context.getNewChangeRequest().getDeveloper().getDeveloperId());
        } catch (EntityRetrievalException e) {
            errorMessages.add(getErrorMessageFromResource("changeRequest.developer.invalid"));
            return errorMessages;
        } catch (Exception e) {
            // This would probably be a NPE, but will be caught by other
            // validators
            return errorMessages;
        }
        return errorMessages;
    }

}
