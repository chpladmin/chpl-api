package gov.healthit.chpl.changerequest.validation;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperActiveValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {

        // Is this a new or existing CR?
        ChangeRequest crToTest = null;
        if (context.getOrigChangeRequest() != null && context.getOrigChangeRequest().getId() != null) {
            try {
                crToTest = context.getValidationDAOs().getChangeRequestDAO().get(context.getOrigChangeRequest().getId());
            } catch (EntityRetrievalException e) {
                // This should be caught be ChangeRequestExistenceValidation
                return true;
            }
        } else {
            crToTest = context.getNewChangeRequest();
        }

        Developer dev;
        try {
            dev = context.getValidationDAOs().getDeveloperDAO().getById(crToTest.getDeveloper().getId());
        } catch (Exception e) {
            // This should be caught be DeveloperExistenceValidation
            return true;
        }

        // Is the developer in a banned or suspended status?
        if (!dev.isNotBannedOrSuspended()) {
            getMessages().add(getErrorMessage("changeRequest.developer.bannedOrSuspended"));
            return false;
        }
        return true;

    }
}
