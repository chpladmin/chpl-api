package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

public interface NotificationManager {
	public NotificationTypeRecipientMapDTO addRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping);
	public RecipientDTO updateRecipient(RecipientDTO toUpdate);
	public List<RecipientWithSubscriptionsDTO> getAll();
	public void deleteRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping);
}
