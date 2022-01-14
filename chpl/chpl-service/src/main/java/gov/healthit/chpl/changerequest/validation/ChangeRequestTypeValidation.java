package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        if (context.getNewChangeRequest().getChangeRequestType() == null
                || context.getNewChangeRequest().getChangeRequestType().getId() == null) {
            errorMessages.add(getErrorMessageFromResource("changeRequest.changeRequestType.required"));
            return errorMessages;
        }

        // Does it exist in the DB?
        try {
            crTypeDAO.getChangeRequestTypeById(context.getNewChangeRequest().getChangeRequestType().getId());
        } catch (EntityRetrievalException e) {
            errorMessages.add(getErrorMessageFromResource("changeRequest.changeRequestType.invalid"));
        }

        return errorMessages;
    }

}
