package gov.healthit.chpl.changerequest.validation;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ContactValidation extends ValidationRule<ChangeRequestValidationContext> {

    @Override
    public boolean isValid(ChangeRequestValidationContext context) {
        if (context.getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequestDeveloperDemographics details = (ChangeRequestDeveloperDemographics) context.getNewChangeRequest().getDetails();
            if (details.getContact() != null) {
                boolean contactComponentsValid = true;
                if (!isNameValid(details.getContact())) {
                    getMessages().add(getErrorMessage("developer.contact.nameRequired"));
                    contactComponentsValid = false;
                }
                if (!isEmailValid(details.getContact())) {
                    getMessages().add(getErrorMessage("developer.contact.emailRequired"));
                    contactComponentsValid = false;
                }
                if (!isPhoneNumberValid(details.getContact())) {
                    getMessages().add(getErrorMessage("developer.contact.phoneRequired"));
                    contactComponentsValid = false;
                }
                return contactComponentsValid;
            }
        }
        return true;
    }

    private boolean isNameValid(PointOfContact contact) {
        return StringUtils.isNotEmpty(contact.getFullName());
    }

    private boolean isEmailValid(PointOfContact contact) {
        return StringUtils.isNotEmpty(contact.getEmail());
    }

    private boolean isPhoneNumberValid(PointOfContact contact) {
        return StringUtils.isNotEmpty(contact.getPhoneNumber());
    }
}
