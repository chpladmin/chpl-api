package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.BooleanUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class SelfDeveloperValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> map = (HashMap) context.getNewChangeRequest().getDetails();
            if (map.containsKey("selfDeveloper") && !isChangeRequestSelfDeveloperValid(map)) {
                getMessages().add(getErrorMessage("changeRequest.details.selfDeveloper.invalid"));
                return false;
            }
        }
        return true;
    }

    private boolean isChangeRequestSelfDeveloperValid(HashMap<String, Object> map) {
        return map.get("selfDeveloper") != null
                && BooleanUtils.toBooleanObject(map.get("selfDeveloper").toString()) != null;
    }
}
