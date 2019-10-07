package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestTypeValidation extends ValidationRule<ChangeRequestValidationContext> {
    private ChangeRequestTypeDAO crTypeDAO;

    @Autowired
    public ChangeRequestTypeValidation(final ChangeRequestTypeDAO crTypeDAO) {
        this.crTypeDAO = crTypeDAO;
    }

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
            crTypeDAO.getChangeRequestTypeById(context.getChangeRequest().getChangeRequestType().getId());
        } catch (EntityRetrievalException e) {
            getMessages().add(getErrorMessage("changeRequest.changeRequestType.invalid"));
            return false;
        }

        return true;
    }

}
