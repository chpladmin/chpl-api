package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestExistenceValidation extends ValidationRule<ChangeRequestValidationContext> {
    private ChangeRequestDAO crDAO;

    @Autowired
    public ChangeRequestExistenceValidation(final ChangeRequestDAO crDAO) {
        this.crDAO = crDAO;
    }

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
            crDAO.get(context.getChangeRequest().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.invalid"));
            return false;
        }

        return true;
    }

}
