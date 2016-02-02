package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.entity.AnnouncementEntity;

import java.util.List;

public interface AnnouncementDAO {
	
	public AnnouncementDTO create(AnnouncementDTO acb) throws EntityRetrievalException, EntityCreationException;

	public void delete(Long acbId);

	public List<AnnouncementDTO> findAll(boolean isLoggedIn);

	public AnnouncementDTO getById(Long id, boolean isLoggedIn) throws EntityRetrievalException;

	public AnnouncementDTO update(AnnouncementDTO contact, boolean includeDeleted) throws EntityRetrievalException;

	public AnnouncementDTO getByIdToUpdate(Long id, boolean includeDeleted) throws EntityRetrievalException;

	public List<AnnouncementDTO> findAllFuture();
	public List<AnnouncementDTO> findAllCurrentAndFuture();

	List<AnnouncementEntity> getAllEntitiesFuture();
	
}
