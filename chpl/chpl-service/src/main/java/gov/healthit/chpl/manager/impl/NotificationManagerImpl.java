package gov.healthit.chpl.manager.impl;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.notification.NotificationTypeRecipientMapDTO;
import gov.healthit.chpl.dto.notification.RecipientDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.notification.SubscriptionDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.NotificationManager;

@Service
public class NotificationManagerImpl implements NotificationManager {
	private static final Logger logger = LogManager.getLogger(NotificationManagerImpl.class);
	
	@Autowired CertificationBodyManager acbManager;
	@Autowired NotificationDAO notificationDao;
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#mapping.subscription.acb, admin))")
	@Transactional
	public NotificationTypeRecipientMapDTO addRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping) {
		if(! notificationDao.hasNotificationType(mapping.getSubscription().getNotificationType(), Util.getCurrentUser().getPermissions())) {
			throw new AccessDeniedException("User " + Util.getUsername() + " does not have permission to create notification with type " + mapping.getSubscription().getNotificationType().getName());
		}
		RecipientDTO recipient = mapping.getRecipient();
		//if no id is passed in, look for an existing recipient with the same email
		//to avoid duplicates
		if(recipient != null && recipient.getId() == null
			&& !StringUtils.isBlank(recipient.getEmailAddress())) {
			String email = recipient.getEmailAddress().trim();
			logger.debug("Looking for existing recipient with email address " + email);
			recipient = notificationDao.findRecipientByEmail(email);
			if(recipient == null) {
				logger.debug("Did not find existing recipient with email address " + email + "... creating new recipient.");
				recipient = notificationDao.createRecipientEmailAddress(mapping.getRecipient().getEmailAddress().trim());
				logger.debug("Created recipient with email address " + email + " and id " + recipient.getId());
			}
		} 
				
		NotificationTypeRecipientMapDTO result = notificationDao.createNotificationMapping(recipient, mapping.getSubscription().getNotificationType(), mapping.getSubscription().getAcb());
		return result;
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public RecipientDTO updateRecipient(Long recipientId, String newEmailAddress) {
		RecipientDTO recipToUpdate = notificationDao.getRecipientById(recipientId);
		recipToUpdate.setEmailAddress(newEmailAddress);
		return updateRecipient(recipToUpdate);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public RecipientDTO updateRecipient(RecipientDTO toUpdate) {
		return notificationDao.updateRecipient(toUpdate);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public boolean recipientEmailExists(String email) {
		return (notificationDao.findRecipientByEmail(email) == null ? false : true);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public List<RecipientWithSubscriptionsDTO> getAll() {
		List<CertificationBodyDTO> acbs = null;
		if(!Util.isUserRoleAdmin()) {
			acbs = acbManager.getAllForUser(true);
		}
		List<RecipientWithSubscriptionsDTO> result = notificationDao.getAllNotificationMappings(Util.getCurrentUser().getPermissions(), acbs);
		return result;
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public RecipientWithSubscriptionsDTO getAllForRecipient(Long recipientId) {
		List<CertificationBodyDTO> acbs = null;
		if(!Util.isUserRoleAdmin()) {
			acbs = acbManager.getAllForUser(true);
		}
		return notificationDao.getAllNotificationMappingsForRecipient(recipientId, Util.getCurrentUser().getPermissions(), acbs);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#mapping.subscription.acb, admin))")
	@Transactional
	public void deleteRecipientNotificationMap(NotificationTypeRecipientMapDTO mapping) {
		if(! notificationDao.hasNotificationType(mapping.getSubscription().getNotificationType(), Util.getCurrentUser().getPermissions())) {
			throw new AccessDeniedException("User " + Util.getUsername() + " does not have permission to create notification with type " + mapping.getSubscription().getNotificationType().getName());
		}
		
		notificationDao.deleteNotificationMapping(mapping.getRecipient(), mapping.getSubscription().getNotificationType(), mapping.getSubscription().getAcb());		
	}
	
	/**
	 * Deletes the recipient as far as the current user would be aware of.
	 * If the recipient has subscriptions that the current user does not have access to,
	 * leave those and leave the recipient but delete any subscriptions that the
	 * current user does have access to. If the recipient is not associated with any
	 * other subscriptions in the system (regardless of current user permissions)
	 * delete the recipient email address as well.
	 * @param recipientId
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	@Transactional
	public void deleteRecipient(Long recipientId) throws EntityNotFoundException {
		RecipientDTO recip = notificationDao.getRecipientById(recipientId);
		if(recip == null) {
			throw new EntityNotFoundException("No recipient exists with id " + recipientId);
		}
		List<CertificationBodyDTO> acbs = null;
		if(!Util.isUserRoleAdmin()) {
			acbs = acbManager.getAllForUser(true);
		}
		//get only the subscriptions the current user should know about and delete those
		RecipientWithSubscriptionsDTO fullRecip = notificationDao.getAllNotificationMappingsForRecipient(recipientId, Util.getCurrentUser().getPermissions(), acbs);
		for(SubscriptionDTO sub : fullRecip.getSubscriptions()) {
			//if the recip has no other subscriptions, dao will take care of deleting it
			notificationDao.deleteNotificationMapping(recip, sub.getNotificationType(), sub.getAcb());
		}
	}
}
