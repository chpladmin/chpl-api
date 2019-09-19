package gov.healthit.chpl.changerequest.validation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperActiveValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Does it exist in the DB?
        DeveloperDTO devDTO;
        try {
            devDTO = context.getDeveloperDAO().getById(context.getChangeRequest().getDeveloper().getDeveloperId());
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
