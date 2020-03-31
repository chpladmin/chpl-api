package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestDeveloperDetailsCreateValidation extends ValidationRule<ChangeRequestValidationContext> {
    @Value("${changerequest.developerDetails}")
    private Long devDetailsChangeRequestType;

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getChangeRequest().getChangeRequestType().getId().equals(devDetailsChangeRequestType)) {
            if (context.getChangeRequest().getDetails() == null) {
                getMessages().add(getErrorMessage("changeRequest.details.invalid"));
                return false;
            } else {
                boolean areDetailsValid = true;
                if (!isChangeRequestSelfDevloperValid((HashMap) context.getChangeRequest().getDetails())) {
                    getMessages().add(getErrorMessage("changeRequest.details.selfDeveloper.missing"));
                    areDetailsValid = false;
                } else if (!isChangeRequestAddressValid((HashMap) context.getChangeRequest().getDetails())) {
                    getMessages().add(getErrorMessage("changeRequest.details.address.missing"));
                    areDetailsValid = false;
                } else if (!isChangeRequestContactValid((HashMap) context.getChangeRequest().getDetails())) {
                    getMessages().add(getErrorMessage("changeRequest.details.contact.missing"));
                    areDetailsValid = false;
                }
                return areDetailsValid;
            }
        }
        return true;
    }

    private boolean isChangeRequestSelfDevloperValid(HashMap<String, Object> map) {
        return map.containsKey("selfDeveloper")
                && !StringUtils.isEmpty(map.get("selfDeveloper").toString());
    }

    private boolean isChangeRequestAddressValid(HashMap<String, Object> map) {
        return map.containsKey("address") && map.get("address") == null;
    }

    private boolean isChangeRequestContactValid(HashMap<String, Object> map) {
        return map.containsKey("contact") && map.get("contact") == null;
    }
}
