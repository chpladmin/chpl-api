package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestCreateValidation extends ValidationRule<ChangeRequestValidationContext> {
    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Value("${changerequest.developerDetails}")
    private Long devDetailsChangeRequestType;

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        if (context.getNewChangeRequest().getChangeRequestType().getId().equals(websiteChangeRequestType)
                && (context.getNewChangeRequest().getDetails() == null
                        || !isChangeRequestWebsiteValid((HashMap) context.getNewChangeRequest().getDetails()))) {
            errorMessages.add(getErrorMessage("changeRequest.details.website.invalid"));
        } else if (context.getNewChangeRequest().getChangeRequestType().getId().equals(devDetailsChangeRequestType)) {
            if (context.getNewChangeRequest().getDetails() == null) {
                errorMessages.add(getErrorMessage("changeRequest.details.invalid"));
            } else {
                HashMap<String, Object> crDetails = (HashMap) context.getNewChangeRequest().getDetails();
                if (!isChangeRequestSelfDevloperValid(crDetails)) {
                    errorMessages.add(getErrorMessage("changeRequest.details.selfDeveloper.missing"));
                } else if (!isChangeRequestAddressValid(crDetails)) {
                    errorMessages.add(getErrorMessage("changeRequest.details.address.missing"));
                } else if (!isChangeRequestContactValid(crDetails)) {
                    errorMessages.add(getErrorMessage("changeRequest.details.contact.missing"));
                }
            }
        }
        return errorMessages;
    }

    private boolean isChangeRequestWebsiteValid(HashMap<String, Object> map) {
        return map.containsKey("website") && !StringUtils.isEmpty(map.get("website").toString());
    }

    private boolean isChangeRequestSelfDevloperValid(HashMap<String, Object> map) {
        if (map.containsKey("selfDeveloper")) {
            return !StringUtils.isEmpty(map.get("selfDeveloper").toString());
        }
        return true;
    }

    private boolean isChangeRequestAddressValid(HashMap<String, Object> map) {
        if (map.containsKey("address")) {
            return map.get("address") != null;
        }
        return true;
    }

    private boolean isChangeRequestContactValid(HashMap<String, Object> map) {
        if (map.containsKey("contact")) {
            return map.get("contact") != null;
        }
        return true;
    }
}
