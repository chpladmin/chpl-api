package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class ChangeRequestDetailsUpdateValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ResourcePermissions resourcePermissions;

    @Autowired
    public ChangeRequestDetailsUpdateValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            if (context.getNewChangeRequest().getDetails() != null) {
                if (isChangeRequestWebsite(context)) {
                    return isChangeRequestWebsiteValid((HashMap) context.getNewChangeRequest().getDetails());
                }
            }
        }

        return true;
    }

    private boolean isChangeRequestWebsiteValid(HashMap<String, String> map) {
        if (!doesKeyExistWithStringData(map, "website")) {
            return false;
        }
        return true;
    }

    private boolean doesKeyExistWithStringData(final HashMap<String, String> map, final String key) {
        return map.containsKey(key) && !StringUtils.isEmpty(map.get(key));
    }

    private boolean isChangeRequestWebsite(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest().getChangeRequestType().getId().equals(websiteChangeRequestType);
    }
}
