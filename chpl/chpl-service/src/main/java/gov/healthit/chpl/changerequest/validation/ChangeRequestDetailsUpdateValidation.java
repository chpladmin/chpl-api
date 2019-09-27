package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestDetailsUpdateValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getChangeRequest().getChangeRequestType().getId().equals(websiteChangeRequestType)
                && (context.getChangeRequest().getDetails() == null
                        || !isChangeRequestWebsiteValid((HashMap) context.getChangeRequest().getDetails()))) {
            getMessages().add(getErrorMessage("changeRequest.details.website.invalid"));
            return false;
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
}
