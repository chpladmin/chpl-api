package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.AnnouncementManager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class AnnouncementManagerImpl extends ApplicationObjectSupport implements AnnouncementManager {

	@Autowired
	private AnnouncementDAO announcementDAO;

	@Autowired
	private ActivityManager activityManager;
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public AnnouncementDTO create(AnnouncementDTO announcement) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		// Create the announcement itself
		AnnouncementDTO result = announcementDAO.create(announcement);
		
		String activityMsg = "Created announcement: " + announcement.getTitle(); 
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, result.getId(), activityMsg, null, result);
		
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public AnnouncementDTO update(AnnouncementDTO announcement) throws EntityRetrievalException, JsonProcessingException, EntityCreationException{

		AnnouncementDTO result = null;
		AnnouncementDTO toUpdate = announcementDAO.getByIdToUpdate(announcement.getId(), false);

		result = announcementDAO.update(announcement, false);

		String activityMsg = "Updated announcement: " + announcement.getTitle();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, result.getId(), activityMsg, toUpdate, result);

		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(AnnouncementDTO announcement) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		//mark the announcement deleted
		announcementDAO.delete(announcement.getId());
		//log announcement delete activity
		String activityMsg = "Deleted announcement: " + announcement.getTitle();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, announcement.getId(), activityMsg, announcement, null);
	}
	
	@Transactional(readOnly = true)
	public List<AnnouncementDTO> getAll() {
		boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
		return announcementDAO.findAll(isLoggedIn);
	}
	
	@Transactional(readOnly = true)
	public AnnouncementDTO getById(Long id) throws EntityRetrievalException {
		boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
		return announcementDAO.getById(id, isLoggedIn);
	}
	
	@Transactional(readOnly = true)
	public AnnouncementDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		return announcementDAO.getByIdToUpdate(id, includeDeleted);
	}
	
	
	public void setAnnouncementDAO(AnnouncementDAO announcementDAO) {
		this.announcementDAO = announcementDAO;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AnnouncementDTO> getAllFuture() {
		return announcementDAO.findAllFuture();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AnnouncementDTO> getAllCurrentAndFuture() {
		return announcementDAO.findAllCurrentAndFuture();
	}
}

