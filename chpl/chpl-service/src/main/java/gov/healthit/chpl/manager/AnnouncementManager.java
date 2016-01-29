package gov.healthit.chpl.manager;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AnnouncementDTO;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;


public interface AnnouncementManager {
	
	public AnnouncementDTO create(AnnouncementDTO announcement) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;
	
	public AnnouncementDTO update(AnnouncementDTO announcement) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	
	public void delete(AnnouncementDTO announcement) throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException;
	
	public List<AnnouncementDTO> getAll();

	public AnnouncementDTO getById(Long id) throws EntityRetrievalException;

	public AnnouncementDTO getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException;

	public List<AnnouncementDTO> getAllFuture();
	
	public List<AnnouncementDTO> getAllCurrentAndFuture();
}
