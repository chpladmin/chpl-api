package gov.healthit.chpl.dto.notification;

import gov.healthit.chpl.dto.CertificationBodyDTO;

public class SubscriptionDTO {
    private CertificationBodyDTO acb;
    private NotificationTypeDTO notificationType;

    public SubscriptionDTO() {
    }

    public CertificationBodyDTO getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBodyDTO acb) {
        this.acb = acb;
    }

    public NotificationTypeDTO getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(final NotificationTypeDTO notificationType) {
        this.notificationType = notificationType;
    }

}
