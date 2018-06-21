package gov.healthit.chpl.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface NotificationDAO {
    RecipientDTO createRecipientEmailAddress(String emailAddress);

    NotificationTypeRecipientMapDTO createNotificationMapping(RecipientDTO recipient, NotificationTypeDTO type,
            CertificationBodyDTO acb);

    boolean hasNotificationType(NotificationTypeDTO type, Set<GrantedPermission> permissions);

    List<NotificationTypeDTO> getAllNotificationTypes(Set<GrantedPermission> permissions);

    List<RecipientWithSubscriptionsDTO> getAllNotificationMappings(Set<GrantedPermission> permissions,
            List<CertificationBodyDTO> acbs);

    List<RecipientWithSubscriptionsDTO> getAllNotificationMappingsForType(Set<GrantedPermission> permissions,
            NotificationTypeConcept notificationType, List<CertificationBodyDTO> acbs);

    RecipientWithSubscriptionsDTO getAllNotificationMappingsForRecipient(Long recipientId,
            Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs) throws EntityRetrievalException;

    RecipientDTO findRecipientByEmail(String email) throws EntityRetrievalException;

    RecipientDTO getRecipientById(Long id) throws EntityRetrievalException;

    RecipientDTO updateRecipient(RecipientDTO updatedRecipient) throws EntityNotFoundException;

    void deleteNotificationMapping(RecipientDTO recipient, NotificationTypeDTO notificationType,
            CertificationBodyDTO acb);
}
