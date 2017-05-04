package gov.healthit.chpl.web.controller.exception;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationException extends ValidationException {
	private static final long serialVersionUID = -6542978782670873229L;
	
	private String objectId;
	private Contact contact;

	public ObjectMissingValidationException() {
		super();
	}
	
	public ObjectMissingValidationException(String message, Contact contact, String objectId) { 
		super(); 
		errorMessages.add(message);
		setContact(contact);
		this.objectId = objectId;
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
