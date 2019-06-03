package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.Collection;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationErrorResponse implements Serializable {
    private static final long serialVersionUID = -2186304673031903240L;
    private Collection<String> errorMessages;
    private Collection<String> warningMessages;
    private String objectId;
    private Contact contact;

    public Collection<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(final Collection<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Collection<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(final Collection<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(final Contact contact) {
        this.contact = contact;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }
}
