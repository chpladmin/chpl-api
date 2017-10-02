package gov.healthit.chpl.entity.surveillance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.ValidationMessageType;


@Entity
@Table(name = "pending_surveillance_validation")
public class PendingSurveillanceValidationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "pending_surveillance_id")
	private Long pendingSurveillanceId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "pending_surveillance_id", insertable = false, updatable = false)
	private PendingSurveillanceEntity pendingSurveillance;

	@Column(name = "message_type")
	@Type(type = "gov.healthit.chpl.entity.PostgresValidationMessageType",
		parameters = {@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.ValidationMessageType")} )
	private ValidationMessageType messageType;

	@Column(name = "message")
	private String message;

	@Column(name = "deleted")
	private Boolean deleted;

	@Column(name = "last_modified_user")
	private Long lastModifiedUser;

	@Column(name = "creation_date", insertable = false, updatable = false)
	private Date creationDate;

	@Column(name = "last_modified_date", insertable = false, updatable = false)
	private Date lastModifiedDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingSurveillanceId() {
		return pendingSurveillanceId;
	}

	public void setPendingSurveillanceId(Long pendingSurveillanceId) {
		this.pendingSurveillanceId = pendingSurveillanceId;
	}

	public PendingSurveillanceEntity getPendingSurveillance() {
		return pendingSurveillance;
	}

	public void setPendingSurveillance(PendingSurveillanceEntity pendingSurveillance) {
		this.pendingSurveillance = pendingSurveillance;
	}

	public ValidationMessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(ValidationMessageType messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
