package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.manager.rules.ValidationRule;

public class WebsiteValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        // Only role we update the website for is ROLE_DEVELOPER
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            // Is there a website?
            if (isChangeRequestWebsiteValid((HashMap) context.getNewChangeRequest().getDetails())) {
                // Is the website valid?
                if (!context.getValidationUtils().isWellFormedUrl(
                        ((HashMap) context.getNewChangeRequest().getDetails()).get("website").toString())) {
                    getMessages().add(getErrorMessage("changeRequest.details.website.invalidFormat"));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isChangeRequestWebsiteValid(HashMap<String, Object> map) {
        // The only value that should be present...
        return map.containsKey("website") && !StringUtils.isEmpty(map.get("website").toString());
    }
}
