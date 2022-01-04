package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperActiveValidation extends ValidationRule<ChangeRequestValidationContext> {
    private DeveloperDAO developerDAO;
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    public DeveloperActiveValidation(final DeveloperDAO developerDAO, final ChangeRequestDAO changeRequestDAO) {
        this.developerDAO = developerDAO;
        this.changeRequestDAO = changeRequestDAO;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {

        // Is this a new or existing CR?
        ChangeRequest crToTest = null;
        if (context.getNewChangeRequest() != null && context.getNewChangeRequest().getId() != null) {
            try {
                crToTest = changeRequestDAO.get(context.getNewChangeRequest().getId());
            } catch (EntityRetrievalException e) {
                // This should be caught be ChangeRequestExistenceValidation
                return true;
            }
        } else {
            crToTest = context.getNewChangeRequest();
        }

        DeveloperDTO devDTO;
        try {
            devDTO = developerDAO.getById(context.getNewChangeRequest().getDeveloper().getDeveloperId());
        } catch (Exception e) {
            // This should be caught be DeveloperExistenceValidation
            return true;
        }

        // Is the developer active?
        if (!devDTO.getStatus().getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
            getMessages().add(getErrorMessage("changeRequest.developer.notActive"));
            return false;
        }
        return true;

    }
}
