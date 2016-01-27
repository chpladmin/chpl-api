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

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class AnnouncementManagerImpl extends ApplicationObjectSupport implements AnnouncementManager {

	@Autowired
	private AnnouncementDAO AnnouncementDAO;

	@Autowired
	private ActivityManager activityManager;
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public AnnouncementDTO create(AnnouncementDTO announcement) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		// Create the announcement itself
		AnnouncementDTO result = AnnouncementDAO.create(announcement);
		
		logger.debug("Created announcement " + result);
		
		String activityMsg = "Created Announcement " + result.getText();
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, result.getId(), activityMsg, null, result);
		
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public AnnouncementDTO update(AnnouncementDTO announcement) throws EntityRetrievalException, JsonProcessingException, EntityCreationException{

		AnnouncementDTO result = null;
		AnnouncementDTO toUpdate = AnnouncementDAO.getByIdToUpdate(announcement.getId(), false);

		result = AnnouncementDAO.update(announcement, false);

		logger.debug("Updated announcement " + announcement);

		String activityMsg = "Updated announcement " + announcement.getText();

		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, result.getId(), activityMsg, toUpdate, result);

		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void undelete(AnnouncementDTO announcement) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
		AnnouncementDTO original = AnnouncementDAO.getByIdToUpdate(announcement.getId(), true);
		announcement.setDeleted(false);
		AnnouncementDTO result = AnnouncementDAO.update(announcement, true);
		
		String activityMsg = "announcement " + original.getText() + " is no longer marked as deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, result.getId(), activityMsg, original, result);	
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(AnnouncementDTO announcement) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
			UserRetrievalException {
		
		//mark the announcement deleted
		AnnouncementDAO.delete(announcement.getId());
		//log announcement delete activity
		String activityMsg = "Deleted announcement " + announcement.getText();
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, announcement.getId(), activityMsg, announcement, null);
	}
	
	@Transactional(readOnly = true)
	public List<AnnouncementDTO> getAll() {
		boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
		return AnnouncementDAO.findAll(isLoggedIn);
	}
	
	@Transactional(readOnly = true)
	public AnnouncementDTO getById(Long id) throws EntityRetrievalException {
		boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
		return AnnouncementDAO.getById(id, isLoggedIn);
	}
	
	@Transactional(readOnly = true)
	public AnnouncementDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		return AnnouncementDAO.getByIdToUpdate(id, includeDeleted);
	}
	
	
	public void setAnnouncementDAO(AnnouncementDAO announcementDAO) {
		this.AnnouncementDAO = announcementDAO;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AnnouncementDTO> getAllFuture() {
		return AnnouncementDAO.findAllFuture();
	}
	
}

