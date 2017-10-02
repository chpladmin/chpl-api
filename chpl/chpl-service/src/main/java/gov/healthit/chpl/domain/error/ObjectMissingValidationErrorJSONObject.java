package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.Collection;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationErrorJSONObject implements Serializable {
	private static final long serialVersionUID = -2186304673031903240L;
	private Collection<String> errorMessages;
	private Collection<String> warningMessages;
	private String objectId;
	private Contact contact;

	public Collection<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(Collection<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public Collection<String> getWarningMessages() {
		return warningMessages;
	}
	public void setWarningMessages(Collection<String> warningMessages) {
		this.warningMessages = warningMessages;
	}
	public Contact getContact() {
		return contact;
	}
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
}
