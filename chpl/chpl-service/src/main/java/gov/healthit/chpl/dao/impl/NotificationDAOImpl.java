package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.entity.notification.NotificationRecipientEntity;
import gov.healthit.chpl.entity.notification.NotificationTypeEntity;
import gov.healthit.chpl.entity.notification.NotificationTypeRecipientMapEntity;
import gov.healthit.chpl.entity.notification.RecipientWithSubscriptionsEntity;

@Repository("notificationDAO")
public class NotificationDAOImpl extends BaseDAOImpl implements NotificationDAO {
	private static final Logger LOGGER = LogManager.getLogger(NotificationDAOImpl.class);

	public RecipientDTO createRecipientEmailAddress(String emailAddress) {
		NotificationRecipientEntity entityToAdd = new NotificationRecipientEntity();
		entityToAdd.setEmail(emailAddress);
		entityToAdd.setLastModifiedUser(Util.getCurrentUser().getId());
		entityToAdd.setDeleted(false);
		entityManager.persist(entityToAdd);
		entityManager.flush();

		return new RecipientDTO(entityToAdd);
	}

	public NotificationTypeRecipientMapDTO createNotificationMapping(RecipientDTO recipient, NotificationTypeDTO type, CertificationBodyDTO acb) {
		NotificationTypeRecipientMapEntity entityToAdd = new NotificationTypeRecipientMapEntity();
		entityToAdd.setRecipientId(recipient.getId());
		entityToAdd.setNotificationTypeId(type.getId());
		if(acb != null && acb.getId() != null) {
			entityToAdd.setAcbId(acb.getId());
		}
		entityToAdd.setLastModifiedUser(Util.getCurrentUser().getId());
		entityToAdd.setDeleted(false);
		entityManager.persist(entityToAdd);
		entityManager.flush();

		Long newMappingId = entityToAdd.getId();
		entityManager.clear();
		NotificationTypeRecipientMapEntity createdEntity = findNotificationMappingById(newMappingId);
		return new NotificationTypeRecipientMapDTO(createdEntity);
	}

	public boolean hasNotificationType(NotificationTypeDTO type, Set<GrantedPermission> permissions) {
		List<String> authorityNames = new ArrayList<String>();
		for(GrantedPermission perm : permissions) {
			authorityNames.add(perm.getAuthority());
		}

		Query query = entityManager.createQuery("SELECT DISTINCT nt "
				+ "FROM NotificationTypeEntity nt "
				+ "LEFT OUTER JOIN FETCH nt.permissions perms "
				+ "LEFT OUTER JOIN FETCH perms.permission perm "
				+ "WHERE nt.deleted <> true "
				+ "AND perm.authority IN (:authorities) "
				+ "AND nt.id = :typeId ", NotificationTypeEntity.class);
		query.setParameter("authorities", authorityNames);
		query.setParameter("typeId", type.getId());

		List<NotificationTypeEntity> availableNotifications = query.getResultList();
		return availableNotifications != null && availableNotifications.size() > 0;
	}

	public List<NotificationTypeDTO> getAllNotificationTypes(Set<GrantedPermission> permissions) {
		List<String> authorityNames = new ArrayList<String>();
		if(permissions != null) {
			for(GrantedPermission perm : permissions) {
				authorityNames.add(perm.getAuthority());
			}
		}

		String hql = "SELECT DISTINCT nt "
				+ "FROM NotificationTypeEntity nt "
				+ "LEFT OUTER JOIN FETCH nt.permissions perms "
				+ "LEFT OUTER JOIN FETCH perms.permission perm "
				+ "WHERE nt.deleted <> true ";
		if(authorityNames != null && authorityNames.size() > 0) {
			hql += "AND perm.authority IN (:authorities)";
		}

		Query query = entityManager.createQuery(hql, NotificationTypeEntity.class);
		if(authorityNames != null && authorityNames.size() > 0) {
			query.setParameter("authorities", authorityNames);
		}

		List<NotificationTypeEntity> notificationTypes = query.getResultList();
		List<NotificationTypeDTO> results = new ArrayList<NotificationTypeDTO>();
		for(NotificationTypeEntity notificationType : notificationTypes) {
			results.add(new NotificationTypeDTO(notificationType));
		}
		return results;
	}

	@Transactional
	public List<RecipientWithSubscriptionsDTO> getAllNotificationMappings(Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs) {
		List<String> authorityNames = new ArrayList<String>();
		if(permissions != null) {
			for(GrantedPermission perm : permissions) {
				authorityNames.add(perm.getAuthority());
			}
		}
		List<Long> acbIds = new ArrayList<Long>();
		if(acbs != null) {
			for(CertificationBodyDTO acb : acbs) {
				acbIds.add(acb.getId());
			}
		}

		List<RecipientWithSubscriptionsEntity> allMappings = findRecipientsWithNotifications(null, authorityNames, null, acbIds);
		List<RecipientWithSubscriptionsDTO> results = new ArrayList<RecipientWithSubscriptionsDTO>();
		for(RecipientWithSubscriptionsEntity mapping : allMappings) {
			RecipientWithSubscriptionsDTO dto = new RecipientWithSubscriptionsDTO(mapping);
			results.add(dto);
		}
		return results;
	}

	@Transactional
	public List<RecipientWithSubscriptionsDTO> getAllNotificationMappingsForType(Set<GrantedPermission> permissions,
			NotificationTypeConcept notificationType, List<CertificationBodyDTO> acbs) {
		List<String> authorityNames = new ArrayList<String>();
		if(permissions != null) {
			for(GrantedPermission perm : permissions) {
				authorityNames.add(perm.getAuthority());
			}
		}
		List<Long> acbIds = new ArrayList<Long>();
		if(acbs != null) {
			for(CertificationBodyDTO acb : acbs) {
				acbIds.add(acb.getId());
			}
		}

		List<RecipientWithSubscriptionsEntity> allMappings = findRecipientsWithNotifications(null, authorityNames, notificationType.getName(), acbIds);
		List<RecipientWithSubscriptionsDTO> results = new ArrayList<RecipientWithSubscriptionsDTO>();
		for(RecipientWithSubscriptionsEntity mapping : allMappings) {
			RecipientWithSubscriptionsDTO dto = new RecipientWithSubscriptionsDTO(mapping);
			results.add(dto);
		}
		return results;
	}

	public RecipientWithSubscriptionsDTO getAllNotificationMappingsForRecipient(
			Long recipientId, Set<GrantedPermission> permissions, List<CertificationBodyDTO> acbs)
		throws EntityRetrievalException {
		List<String> authorityNames = new ArrayList<String>();
		if(permissions != null) {
			for(GrantedPermission perm : permissions) {
				authorityNames.add(perm.getAuthority());
			}
		}
		List<Long> acbIds = new ArrayList<Long>();
		if(acbs != null) {
			for(CertificationBodyDTO acb : acbs) {
				acbIds.add(acb.getId());
			}
		}

		List<RecipientWithSubscriptionsEntity> allMappings = findRecipientsWithNotifications(recipientId, authorityNames, null, acbIds);
		if(allMappings == null || allMappings.size() == 0) {
			String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("recipient.notFound"), LocaleContextHolder.getLocale()));
			throw new EntityRetrievalException(msg);
		} else {
			return new RecipientWithSubscriptionsDTO(allMappings.get(0));
		}
	}

	public RecipientDTO findRecipientByEmail(String email) throws EntityRetrievalException {
		Query query = entityManager.createQuery("SELECT recip "
				+ "FROM NotificationRecipientEntity recip "
				+ "WHERE recip.deleted <> true "
				+ "AND UPPER(recip.email) = :email",
				NotificationRecipientEntity.class);
		query.setParameter("email", email.toUpperCase());

		List<NotificationRecipientEntity> matchedRecipients = query.getResultList();
		if(matchedRecipients == null || matchedRecipients.size() == 0) {
			String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("recipient.notFound"), LocaleContextHolder.getLocale()));
			throw new EntityRetrievalException(msg);
		} else {
			return new RecipientDTO(matchedRecipients.get(0));
		}
	}

	public RecipientDTO getRecipientById(Long id) throws EntityRetrievalException {
		Query query = entityManager.createQuery("SELECT recip "
				+ "FROM NotificationRecipientEntity recip "
				+ "WHERE recip.deleted <> true "
				+ "AND recip.id = :id",
				NotificationRecipientEntity.class);
		query.setParameter("id", id);

		List<NotificationRecipientEntity> matchedRecipients = query.getResultList();
		if(matchedRecipients == null || matchedRecipients.size() == 0) {
			String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("recipient.notFound"), LocaleContextHolder.getLocale()));
			throw new EntityRetrievalException(msg);
		} else {
			return new RecipientDTO(matchedRecipients.get(0));
		}
	}

	public RecipientDTO updateRecipient(RecipientDTO updatedRecipient) throws EntityNotFoundException {
		NotificationRecipientEntity entityToUpdate = entityManager.find(NotificationRecipientEntity.class, updatedRecipient.getId());
		if(entityToUpdate == null) {
			throw new EntityNotFoundException("No recipient was found with id " + updatedRecipient.getId());
		}
		entityToUpdate.setEmail(updatedRecipient.getEmailAddress());
		entityToUpdate.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(entityToUpdate);
		entityManager.flush();

		return new RecipientDTO(entityToUpdate);
	}

	public void deleteNotificationMapping(RecipientDTO recipient, NotificationTypeDTO notificationType, CertificationBodyDTO acb) {
		Long acbId = (acb == null ? null : acb.getId());
		List<NotificationTypeRecipientMapEntity> foundMappings = findNotificationMapping(recipient.getId(), notificationType.getId(), acbId);
		if(foundMappings != null && foundMappings.size() > 0) {
			for(NotificationTypeRecipientMapEntity mapping : foundMappings) {
				mapping.setLastModifiedUser(Util.getCurrentUser().getId());
				mapping.setDeleted(true);
				entityManager.merge(mapping);
			}
		} else {
			LOGGER.error("Could not find notification-recipient mapping with recipient id " + recipient.getId() + ", notification type id " + notificationType.getId() + ", and acb id " + acbId);
		}

		//if the recipient is not signed up for any more notifications
		//then delete them
		List<NotificationTypeRecipientMapEntity> otherNotificationsForRecip =
				findNotificationMappingsForRecipient(recipient.getId());
		if(otherNotificationsForRecip == null || otherNotificationsForRecip.size() == 0) {
			NotificationRecipientEntity recipToDelete = entityManager.find(NotificationRecipientEntity.class, recipient.getId());
			recipToDelete.setDeleted(true);
			recipToDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(recipToDelete);
		}

		entityManager.flush();
		entityManager.clear();
	}

	private List<RecipientWithSubscriptionsEntity> findRecipientsWithNotifications(Long recipId, List<String> authorityNames, String notificationTypeName, List<Long> acbIds) {
		String hql = "SELECT DISTINCT recip "
				+ "FROM RecipientWithSubscriptionsEntity recip "
				+ "LEFT OUTER JOIN FETCH recip.subscriptions subs "
				+ "LEFT OUTER JOIN FETCH subs.acb acb "
				+ "LEFT OUTER JOIN FETCH subs.notificationType nt "
				+ "LEFT OUTER JOIN FETCH nt.permissions perms "
				+ "LEFT OUTER JOIN FETCH perms.permission perm "
				+ "WHERE recip.deleted <> true ";
		if(recipId != null) {
			hql += " AND recip.id = :recipId ";
		}
		if(authorityNames != null && authorityNames.size() > 0) {
			hql += " AND perm.authority IN (:authorities) ";
		}
		if(!StringUtils.isEmpty(notificationTypeName)) {
			hql += " AND UPPER(nt.name) = :notificationType";
		}
		if(acbIds != null && acbIds.size() > 0) {
			hql += " AND acb.id IN (:acbIds) ";
		}

		Query query = entityManager.createQuery(hql, RecipientWithSubscriptionsEntity.class);
		if(recipId != null) {
			query.setParameter("recipId", recipId);
		}
		if(authorityNames != null && authorityNames.size() > 0) {
			query.setParameter("authorities", authorityNames);
		}
		if(!StringUtils.isEmpty(notificationTypeName)) {
			query.setParameter("notificationType", notificationTypeName.toUpperCase());
		}
		if(acbIds != null && acbIds.size() > 0) {
			query.setParameter("acbIds", acbIds);
		}

		return query.getResultList();
	}


	private NotificationTypeRecipientMapEntity findNotificationMappingById(Long mappingId) {
		Query query = entityManager.createQuery("SELECT DISTINCT mapping "
				+ "FROM NotificationTypeRecipientMapEntity mapping "
				+ "LEFT OUTER JOIN FETCH mapping.recipient "
				+ "LEFT OUTER JOIN FETCH mapping.acb "
				+ "LEFT OUTER JOIN FETCH mapping.notificationType "
				+ "WHERE mapping.deleted <> true "
				+ "AND mapping.id = :mappingId",
				NotificationTypeRecipientMapEntity.class);
		query.setParameter("mappingId", mappingId);

		List<NotificationTypeRecipientMapEntity> results = query.getResultList();
		if(results != null && results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	private List<NotificationTypeRecipientMapEntity> findNotificationMappingsForRecipient(Long recipientId) {
		Query query = entityManager.createQuery("SELECT DISTINCT recip "
				+ "FROM NotificationTypeRecipientMapEntity recip "
				+ "LEFT OUTER JOIN FETCH recip.recipient "
				+ "LEFT OUTER JOIN FETCH recip.acb "
				+ "LEFT OUTER JOIN FETCH recip.notificationType "
				+ "WHERE recip.deleted <> true "
				+ "AND recip.recipientId = :recipientId",
				NotificationTypeRecipientMapEntity.class);
		query.setParameter("recipientId", recipientId);

		return query.getResultList();
	}

	private List<NotificationTypeRecipientMapEntity> findNotificationMapping(Long recipientId, Long notificationTypeId, Long acbId) {
		String hql = "SELECT DISTINCT mapping "
				+ "FROM NotificationTypeRecipientMapEntity mapping "
				+ "LEFT OUTER JOIN FETCH mapping.recipient recipient "
				+ "LEFT OUTER JOIN FETCH mapping.acb acb "
				+ "LEFT OUTER JOIN FETCH mapping.notificationType type "
				+ "WHERE mapping.deleted <> true "
				+ "AND type.id = :notificationTypeId ";
		if(recipientId != null) {
			hql += " AND recipient.id = :recipientId ";
		} else {
			hql += " AND mapping.recipientId IS NULL ";
		}

		if(acbId != null) {
			hql += " AND acb.id = :acbId";
		} else {
			hql += "AND mapping.acbId IS NULL";
		}

		Query query = entityManager.createQuery(hql, NotificationTypeRecipientMapEntity.class);
		query.setParameter("notificationTypeId", notificationTypeId);
		if(recipientId != null) {
			query.setParameter("recipientId", recipientId);
		}
		if(acbId != null) {
			query.setParameter("acbId", acbId);
		}

		return query.getResultList();
	}
}
