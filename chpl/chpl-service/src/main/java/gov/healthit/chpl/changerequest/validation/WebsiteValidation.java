package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ValidationUtils;

@Component
public class WebsiteValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;
    private ValidationUtils validationUtils;

    @Autowired
    public WebsiteValidation(ResourcePermissions resourcePermissions, ValidationUtils validationUtils) {
        this.resourcePermissions = resourcePermissions;
        this.validationUtils = validationUtils;
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        // Only role we update the website for is ROLE_DEVELOPER
        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            // Is there a website?
            if (isChangeRequestWebsiteValid((HashMap) context.getNewChangeRequest().getDetails())) {
                // Is the website valid?
                if (!validationUtils.isWellFormedUrl(
                        ((HashMap) context.getNewChangeRequest().getDetails()).get("website").toString())) {
                    errorMessages.add(getErrorMessageFromResource("changeRequest.details.website.invalidFormat"));
                }
            }
        }
        return errorMessages;
    }

    private boolean isChangeRequestWebsiteValid(HashMap<String, Object> map) {
        // The only value that should be present...
        return map.containsKey("website") && !StringUtils.isEmpty(map.get("website").toString());
    }
}
