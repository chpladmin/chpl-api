package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class ChangeRequestDetailsUpdateValidation extends ValidationRule<ChangeRequestValidationContext> {

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

    private boolean isChangeRequestWebsiteValid(HashMap<String, String> map) {
        // The only value that should be present...
        if (!doesKeyExistWithStringData(map, "website")) {
            return false;
        }
        if (!doesKeyExistWithIntegerData(map, "id")) {
            return false;
        }
        return true;
    }

    private boolean doesKeyExistWithStringData(final HashMap<String, String> map, final String key) {
        return map.containsKey(key) && !StringUtils.isEmpty(map.get(key));
    }

    private boolean doesKeyExistWithIntegerData(final HashMap<String, String> map, final String key) {
        return map.containsKey(key) && map.get(key) != null;
    }
}
