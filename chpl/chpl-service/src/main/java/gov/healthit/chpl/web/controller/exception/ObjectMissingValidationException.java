package gov.healthit.chpl.web.controller.exception;

import java.util.Date;

import gov.healthit.chpl.domain.Contact;

public class ObjectMissingValidationException extends ValidationException {
	private static final long serialVersionUID = -6542978782670873229L;
	
	private String objectId;
	private Contact contact;
	private Date startDate;
	private Date endDate;

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
