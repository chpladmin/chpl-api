package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperActiveValidation extends ValidationRule<ChangeRequestValidationContext> {
    private DeveloperDAO developerDAO;

    @Autowired
    public DeveloperActiveValidation(final DeveloperDAO developerDAO) {
        this.developerDAO = developerDAO;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Does it exist in the DB?
        DeveloperDTO devDTO;
        try {
            devDTO = developerDAO.getById(context.getChangeRequest().getDeveloper().getDeveloperId());
        } catch (Exception e) {
            // This should be handled by another validator
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
