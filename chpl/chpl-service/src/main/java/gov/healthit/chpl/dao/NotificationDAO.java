package gov.healthit.chpl.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("notificationDAO")
public interface NotificationDAO {
	public RecipientDTO createRecipientEmailAddress(String emailAddress);
	public NotificationTypeRecipientMapDTO createNotificationMapping(RecipientDTO recipient, NotificationTypeDTO type, CertificationBodyDTO acb);
	public boolean hasNotificationType(NotificationTypeDTO type, Set<GrantedPermission> permissions);
	public List<NotificationTypeDTO> getAllNotificationTypes(Set<GrantedPermission> permissions);
	public List<RecipientWithSubscriptionsDTO> getAllNotificationMappings(Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs);
	public RecipientDTO findRecipientByEmail(String email);
	public RecipientDTO updateRecipient(RecipientDTO updatedRecipient) throws EntityNotFoundException;
	public void deleteNotificationMapping(RecipientDTO recipient, NotificationTypeDTO notificationType, CertificationBodyDTO acb);
}
