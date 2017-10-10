package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AnnouncementDTO;

public interface AnnouncementManager {

    AnnouncementDTO create(AnnouncementDTO announcement)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    AnnouncementDTO update(AnnouncementDTO announcement)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void delete(AnnouncementDTO announcement)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException;

    List<AnnouncementDTO> getAll();

    AnnouncementDTO getById(Long id) throws EntityRetrievalException;

    AnnouncementDTO getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException;

    List<AnnouncementDTO> getAllFuture();

    List<AnnouncementDTO> getAllCurrentAndFuture();
}
