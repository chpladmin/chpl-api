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

public interface NotificationDAO {
	public RecipientDTO createRecipientEmailAddress(String emailAddress);
	public NotificationTypeRecipientMapDTO createNotificationMapping(RecipientDTO recipient, NotificationTypeDTO type, CertificationBodyDTO acb);
	public boolean hasNotificationType(NotificationTypeDTO type, Set<GrantedPermission> permissions);
	public List<NotificationTypeDTO> getAllNotificationTypes(Set<GrantedPermission> permissions);
	public List<RecipientWithSubscriptionsDTO> getAllNotificationMappings(Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs);
	public List<RecipientWithSubscriptionsDTO> getAllNotificationMappingsForType(Set<GrantedPermission> permissions, 
			NotificationTypeConcept notificationType, List<CertificationBodyDTO> acbs);
	public RecipientWithSubscriptionsDTO getAllNotificationMappingsForRecipient(
			Long recipientId, Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs)
			throws EntityRetrievalException;
	public RecipientDTO findRecipientByEmail(String email) throws EntityRetrievalException;
	public RecipientDTO getRecipientById(Long id) throws EntityRetrievalException;
	public RecipientDTO updateRecipient(RecipientDTO updatedRecipient) throws EntityNotFoundException;
	public void deleteNotificationMapping(RecipientDTO recipient, NotificationTypeDTO notificationType, CertificationBodyDTO acb);
}
