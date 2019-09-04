package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestDetailsCreateValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Do we know the cr type?
        ChangeRequestTypeValidation crTypeValidation = new ChangeRequestTypeValidation();
        if (!crTypeValidation.isValid(context)) {
            getMessages().add(getErrorMessage("changeRequest.details.cannotDetermineType"));
            return false;
        }

        // TODO How do I get these values in here???
        if (context.getChangeRequest().getChangeRequestType().getId().equals(1l)
                && (context.getChangeRequest().getDetails() == null
                        || !isChangeRequestWebsiteValid((HashMap) context.getChangeRequest().getDetails()))) {
            getMessages().add(getErrorMessage("changeRequest.details.website.invalid"));
            return false;
        }

        return true;
    }

    public boolean isChangeRequestWebsiteValid(HashMap<String, String> map) {
        // The only value that should be present...
        return map.containsKey("website") && !StringUtils.isEmpty(map.get("website"));
    }
}
