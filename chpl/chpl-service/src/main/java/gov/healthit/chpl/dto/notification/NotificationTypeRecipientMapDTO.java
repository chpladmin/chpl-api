package gov.healthit.chpl.dto.notification;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.notification.NotificationTypeRecipientMapEntity;

public class NotificationTypeRecipientMapDTO {
    private Long id;
    private RecipientDTO recipient;
    private SubscriptionDTO subscription;

    public NotificationTypeRecipientMapDTO() {
    }

    public NotificationTypeRecipientMapDTO(NotificationTypeRecipientMapEntity entity) {
        this.id = entity.getId();
        if (entity.getRecipient() != null) {
            this.recipient = new RecipientDTO(entity.getRecipient());
        } else {
            this.recipient = new RecipientDTO();
            this.recipient.setId(entity.getRecipientId());
        }

        SubscriptionDTO subscription = new SubscriptionDTO();
        if (entity.getNotificationType() != null) {
            subscription.setNotificationType(new NotificationTypeDTO(entity.getNotificationType()));
        } else {
            NotificationTypeDTO notificationType = new NotificationTypeDTO();
            notificationType.setId(entity.getNotificationTypeId());
            subscription.setNotificationType(notificationType);
        }

        if (entity.getAcb() != null) {
            subscription.setAcb(new CertificationBodyDTO(entity.getAcb()));
        } else if (entity.getAcbId() != null) {
            CertificationBodyDTO acb = new CertificationBodyDTO();
            acb.setId(entity.getAcbId());
            subscription.setAcb(acb);
        }
        this.subscription = subscription;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public RecipientDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(final RecipientDTO recipient) {
        this.recipient = recipient;
    }

    public SubscriptionDTO getSubscription() {
        return subscription;
    }

    public void setSubscription(final SubscriptionDTO subscription) {
        this.subscription = subscription;
    }

}
