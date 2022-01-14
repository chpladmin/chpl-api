package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ContactValidation extends ValidationRule<ChangeRequestValidationContext> {

    private ResourcePermissions resourcePermissions;

    @Autowired
    public ContactValidation(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        List<String> errorMessages = new ArrayList<String>();

        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getNewChangeRequest().getDetails();
            if (details.containsKey("contact")) {
                PointOfContact crContact = new PointOfContact((HashMap<String, Object>) details.get("contact"));
                if (!isNameValid(crContact)) {
                    errorMessages.add(getErrorMessage("developer.contact.nameRequired"));
                }
                if (!isEmailValid(crContact)) {
                    errorMessages.add(getErrorMessage("developer.contact.emailRequired"));
                }
                if (!isPhoneNumberValid(crContact)) {
                    errorMessages.add(getErrorMessage("developer.contact.phoneRequired"));
                }
            }
        }
        return errorMessages;
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
