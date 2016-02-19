package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ContactDTO;

public class Contact {
	private Long contactId;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String title;
	
	public Contact() {}
	
	public Contact(ContactDTO dto) {
		this.contactId = dto.getId();
		this.firstName = dto.getFirstName();
		this.lastName = dto.getLastName();
		this.email = dto.getEmail();
		this.phoneNumber = dto.getPhoneNumber();
		this.title = dto.getTitle();
	}

	public Long getContactId() {
		return contactId;
	}

	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
