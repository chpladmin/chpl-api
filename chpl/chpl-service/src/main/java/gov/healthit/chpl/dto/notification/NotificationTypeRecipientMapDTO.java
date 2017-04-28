package gov.healthit.chpl.dto.notification;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.notification.NotificationTypeRecipientMapEntity;

public class NotificationTypeRecipientMapDTO {
	private Long id;
	private RecipientDTO recipient;
	private SubscriptionDTO notification;
	
	public NotificationTypeRecipientMapDTO() {}
	
	public NotificationTypeRecipientMapDTO(NotificationTypeRecipientMapEntity entity) {
		this.id = entity.getId();
		if(entity.getRecipient() != null) {
			this.recipient = new RecipientDTO(entity.getRecipient());
		} else {
			this.recipient = new RecipientDTO();
			this.recipient.setId(entity.getRecipientId());
		}
		
		SubscriptionDTO notification = new SubscriptionDTO();
		if(entity.getNotificationType() != null) {
			notification.setNotificationType(new NotificationTypeDTO(entity.getNotificationType()));
		} else {
			NotificationTypeDTO notificationType = new NotificationTypeDTO();
			notificationType.setId(entity.getNotificationTypeId());
			notification.setNotificationType(notificationType);
		}
		
		if(entity.getAcb() != null) {
			notification.setAcb(new CertificationBodyDTO(entity.getAcb()));
		} else if(entity.getAcbId() != null) {
			CertificationBodyDTO acb = new CertificationBodyDTO();
			acb.setId(entity.getAcbId());
			notification.setAcb(acb);
		}
		this.notification = notification;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RecipientDTO getRecipient() {
		return recipient;
	}

	public void setRecipient(RecipientDTO recipient) {
		this.recipient = recipient;
	}

	public SubscriptionDTO getNotification() {
		return notification;
	}

	public void setNotification(SubscriptionDTO notification) {
		this.notification = notification;
	}

}
