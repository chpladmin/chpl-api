package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.entity.AnnouncementEntity;

public interface AnnouncementDAO {

    AnnouncementDTO create(AnnouncementDTO acb) throws EntityRetrievalException, EntityCreationException;

    void delete(Long acbId);

    List<AnnouncementDTO> findAll(boolean isLoggedIn);

    AnnouncementDTO getById(Long id, boolean isLoggedIn) throws EntityRetrievalException;

    AnnouncementDTO update(AnnouncementDTO contact, boolean includeDeleted) throws EntityRetrievalException;

    AnnouncementDTO getByIdToUpdate(Long id, boolean includeDeleted) throws EntityRetrievalException;

    List<AnnouncementDTO> findAllFuture();

    List<AnnouncementDTO> findAllCurrentAndFuture();

    List<AnnouncementEntity> getAllEntitiesFuture();

}
