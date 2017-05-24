package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.ContactDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Contact implements Serializable {
	private static final long serialVersionUID = 5378524206189674741L;
	
	/**
	 * Contact internal ID
	 */
	@XmlElement(required = true)
	private Long contactId;
	
	/**
	 * Contact first name
	 */
	@XmlElement(required = false, nillable=true)
	private String firstName;
	
	/**
	 * Contact last name
	 */
	@XmlElement(required = true)
	private String lastName;
	
	/**
	 * Contact email address
	 */
	@XmlElement(required = true)
	private String email;
	
	/**
	 * Contact phone number
	 */
	@XmlElement(required = true)
	private String phoneNumber;
	
	/**
	 * Contact title (Ms., Mr., Dr., etc)
	 */
	@XmlElement(required = false, nillable=true)
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
