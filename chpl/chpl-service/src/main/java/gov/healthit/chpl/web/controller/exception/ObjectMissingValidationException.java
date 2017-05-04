package gov.healthit.chpl.web.controller.exception;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationException extends ValidationException {
	private static final long serialVersionUID = -6542978782670873229L;
	
	private Contact contact;

	public ObjectMissingValidationException() {
		super();
	}
	
	public ObjectMissingValidationException(String message, Contact contact) { 
		super(); 
		errorMessages.add(message);
		setContact(contact);
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
}
