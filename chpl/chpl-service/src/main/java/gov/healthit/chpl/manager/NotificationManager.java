package gov.healthit.chpl.manager;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

public interface NotificationManager {
	public RecipientDTO createRecipient(RecipientDTO toCreate) throws EntityCreationException;
	public NotificationTypeRecipientMapDTO addRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping) throws EntityNotFoundException;
	public RecipientDTO updateRecipient(Long recipientId, String newEmailAddress);
	public RecipientDTO updateRecipient(RecipientDTO toUpdate);
	public boolean recipientEmailExists(String email);
	public List<RecipientWithSubscriptionsDTO> getAll();
	public RecipientWithSubscriptionsDTO getAllForRecipient(Long recipientId);
	public void deleteRecipient(Long recipientId) throws EntityNotFoundException;
	public void deleteRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping);
}
