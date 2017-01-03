package gov.healthit.chpl.auth.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="contact")
public class UserContactEntity {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="contact_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Basic(optional = false)
	@Column(name="first_name", nullable=false)
	private String firstName;
	
	@Basic(optional = false)
	@Column(name="last_name", nullable=false)
	private String lastName;
	
	@Basic(optional = false)
	@Column(name="email", nullable=false)
	private String email;
	
	@Basic(optional = false)
	@Column(name="phone_number", nullable=false)
	private String phoneNumber;
	
	@Column(name="title")
	private String title;
	
	@Column(name="signature_date")
	private Date signatureDate;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name = "last_modified_date")
	private Date lastModifiedDate;
	
	@Column(name = "deleted")
	private Boolean deleted;
	
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

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Long getId() {
		return id;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
}
