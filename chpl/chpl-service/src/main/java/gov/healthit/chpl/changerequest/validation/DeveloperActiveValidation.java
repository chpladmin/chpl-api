package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperActiveValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {

        // Is this a new or existing CR?
        ChangeRequest crToTest = null;
        if (context.getChangeRequest() != null && context.getChangeRequest().getId() != null) {
            try {
                crToTest = context.getChangeRequestDAO().get(context.getChangeRequest().getId());
            } catch (EntityRetrievalException e) {
                // This should be caught be ChangeRequestExistenceValidation
                return true;
            }
        } else {
            crToTest = context.getChangeRequest();
        }

        DeveloperDTO devDTO;
        try {
            devDTO = context.getDeveloperDAO().getById(crToTest.getDeveloper().getDeveloperId());
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
