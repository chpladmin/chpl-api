package gov.healthit.chpl.dto.notification;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.notification.NotificationTypeRecipientMapEntity;
import gov.healthit.chpl.entity.notification.RecipientWithSubscriptionsEntity;

public class RecipientWithSubscriptionsDTO {
    private Long id;
    private String email;
    private List<SubscriptionDTO> subscriptions;

    public RecipientWithSubscriptionsDTO() {
        subscriptions = new ArrayList<SubscriptionDTO>();
    }

    public RecipientWithSubscriptionsDTO(RecipientWithSubscriptionsEntity entity) {
        this();
        this.id = entity.getId();
        this.email = entity.getEmail();

        if (entity.getSubscriptions() != null && entity.getSubscriptions().size() > 0) {
            for (NotificationTypeRecipientMapEntity notificationEntity : entity.getSubscriptions()) {
                SubscriptionDTO subscription = new SubscriptionDTO();
                if (notificationEntity.getNotificationType() != null) {
                    subscription.setNotificationType(new NotificationTypeDTO(notificationEntity.getNotificationType()));
                } else {
                    NotificationTypeDTO notificationType = new NotificationTypeDTO();
                    notificationType.setId(notificationEntity.getNotificationTypeId());
                    subscription.setNotificationType(notificationType);
                }

                if (notificationEntity.getAcb() != null) {
                    subscription.setAcb(new CertificationBodyDTO(notificationEntity.getAcb()));
                } else if (notificationEntity.getAcbId() != null) {
                    CertificationBodyDTO acb = new CertificationBodyDTO();
                    acb.setId(notificationEntity.getAcbId());
                    subscription.setAcb(acb);
                }
                this.subscriptions.add(subscription);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<SubscriptionDTO> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionDTO> subscriptions) {
        this.subscriptions = subscriptions;
    }

}
