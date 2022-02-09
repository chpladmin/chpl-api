package gov.healthit.chpl.changerequest.validation;

import java.util.HashMap;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ContactValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getNewChangeRequest().getDetails();
            if (details.containsKey("contact")) {
                PointOfContact crContact = new PointOfContact((HashMap<String, Object>) details.get("contact"));
                boolean contactComponentsValid = true;
                if (!isNameValid(crContact)) {
                    getMessages().add(getErrorMessage("developer.contact.nameRequired"));
                    contactComponentsValid = false;
                }
                if (!isEmailValid(crContact)) {
                    getMessages().add(getErrorMessage("developer.contact.emailRequired"));
                    contactComponentsValid = false;
                }
                if (!isPhoneNumberValid(crContact)) {
                    getMessages().add(getErrorMessage("developer.contact.phoneRequired"));
                    contactComponentsValid = false;
                }
                return contactComponentsValid;
            }
        }
        return true;
    }

    private boolean isNameValid(PointOfContact contact) {
        return !StringUtils.isEmpty(contact.getFullName());
    }

    private boolean isEmailValid(PointOfContact contact) {
        return !StringUtils.isEmpty(contact.getEmail());
    }

    private boolean isPhoneNumberValid(PointOfContact contact) {
        return !StringUtils.isEmpty(contact.getPhoneNumber());
    }
}
