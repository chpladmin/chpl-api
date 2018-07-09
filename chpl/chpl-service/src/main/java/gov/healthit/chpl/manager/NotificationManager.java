package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface NotificationManager {
    RecipientDTO createRecipient(RecipientDTO toCreate) throws EntityCreationException;

    NotificationTypeRecipientMapDTO addRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping)
            throws EntityRetrievalException;

    RecipientDTO updateRecipient(Long recipientId, String newEmailAddress) throws EntityRetrievalException;

    RecipientDTO updateRecipient(RecipientDTO toUpdate);

    boolean recipientEmailExists(String email);

    List<RecipientWithSubscriptionsDTO> getAll();

    RecipientWithSubscriptionsDTO getAllForRecipient(Long recipientId) throws EntityRetrievalException;

    void deleteRecipient(Long recipientId) throws EntityRetrievalException;

    void deleteRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping);
}
