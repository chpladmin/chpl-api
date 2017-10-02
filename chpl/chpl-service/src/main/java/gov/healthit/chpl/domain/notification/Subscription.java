package gov.healthit.chpl.domain.notification;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;

public class Subscription {
    private CertificationBody acb;
    private NotificationType notificationType;

    public Subscription() {
    }

    public Subscription(SubscriptionDTO dto) {
        if (dto.getAcb() != null) {
            this.acb = new CertificationBody(dto.getAcb());
        }
        if (dto.getNotificationType() != null) {
            this.notificationType = new NotificationType(dto.getNotificationType());
        }
    }

    public CertificationBody getAcb() {
        return acb;
    }

    public void setAcb(CertificationBody acb) {
        this.acb = acb;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
