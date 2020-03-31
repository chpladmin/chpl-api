package gov.healthit.chpl.changerequest.validation;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.JSONUtils;
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
    public boolean isValid(ChangeRequestValidationContext context) {
        if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            HashMap<String, Object> details = (HashMap) context.getChangeRequest().getDetails();
            if (details.containsKey("contact")) {
                Contact crContact = null;
                try {
                    crContact = JSONUtils.fromJSON(details.get("contact").toString(), Contact.class);
                } catch (IOException ex) {
                    LOGGER.error("Could not parse " + details.get("contact") + " as an Contact object.", ex);
                    return false;
                }
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

    private boolean isNameValid(Contact contact) {
        return !StringUtils.isEmpty(contact.getFullName());
    }

    private boolean isEmailValid(Contact contact) {
        return !StringUtils.isEmpty(contact.getEmail());
    }

    private boolean isPhoneNumberValid(Contact contact) {
        return !StringUtils.isEmpty(contact.getPhoneNumber());
    }
}
