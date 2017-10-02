package gov.healthit.chpl.entity.notification;

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

import gov.healthit.chpl.entity.CertificationBodyEntity;


@Entity
@Table(name = "notification_type_recipient_map")
public class NotificationTypeRecipientMapEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "recipient_id")
	private Long recipientId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient_id", insertable = false, updatable = false)
	private NotificationRecipientEntity recipient;

	@Column(name = "notification_type_id")
	private Long notificationTypeId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_type_id", insertable = false, updatable = false)
	private NotificationTypeEntity notificationType;

	@Column(name = "acb_id")
	private Long acbId;

	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "acb_id", insertable = false, updatable = false)
	private CertificationBodyEntity acb;

	@Column( name = "deleted")
	private Boolean deleted;

	@Column( name = "last_modified_user")
	private Long lastModifiedUser;

	@Column( name = "creation_date", insertable = false, updatable = false  )
	private Date creationDate;

	@Column( name = "last_modified_date", insertable = false, updatable = false )
	private Date lastModifiedDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}

	public NotificationRecipientEntity getRecipient() {
		return recipient;
	}

	public void setRecipient(NotificationRecipientEntity recipient) {
		this.recipient = recipient;
	}

	public Long getNotificationTypeId() {
		return notificationTypeId;
	}

	public void setNotificationTypeId(Long notificationTypeId) {
		this.notificationTypeId = notificationTypeId;
	}

	public NotificationTypeEntity getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationTypeEntity notificationType) {
		this.notificationType = notificationType;
	}

	public Long getAcbId() {
		return acbId;
	}

	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}

	public CertificationBodyEntity getAcb() {
		return acb;
	}

	public void setAcb(CertificationBodyEntity acb) {
		this.acb = acb;
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
