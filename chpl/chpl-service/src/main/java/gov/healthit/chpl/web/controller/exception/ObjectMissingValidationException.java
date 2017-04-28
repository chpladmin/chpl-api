package gov.healthit.chpl.web.controller.exception;

import java.util.HashSet;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationException extends ValidationException {
	private static final long serialVersionUID = -6542978782670873229L;
	
	private Contact contact;

	public ObjectMissingValidationException(String message, Contact lastModifiedUser) { 
		super(); 
		errorMessages = new HashSet<String>();
		errorMessages.add(message);
		warningMessages = new HashSet<String>();
		setContact(lastModifiedUser);
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
}
