package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class SelfDeveloperValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public SelfDeveloperValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> map = (HashMap) context.getChangeRequest().getDetails();
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
