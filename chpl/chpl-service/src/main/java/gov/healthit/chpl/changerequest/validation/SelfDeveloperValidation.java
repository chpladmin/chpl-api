package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> map = (HashMap) context.getNewChangeRequest().getDetails();
            if (map.containsKey("selfDeveloper") && !isChangeRequestSelfDeveloperValid(map)) {
                errorMessages.add(getErrorMessageFromResource("changeRequest.details.selfDeveloper.invalid"));
            }
        }
        return errorMessages;
    }

    private boolean isChangeRequestSelfDeveloperValid(HashMap<String, Object> map) {
        return map.get("selfDeveloper") != null
                && BooleanUtils.toBooleanObject(map.get("selfDeveloper").toString()) != null;
    }
}
