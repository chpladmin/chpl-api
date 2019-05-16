package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface AnnouncementManager {

    AnnouncementDTO create(AnnouncementDTO announcement)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    AnnouncementDTO update(AnnouncementDTO announcement)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void delete(AnnouncementDTO announcement)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException;

    List<AnnouncementDTO> getAll();

    AnnouncementDTO getById(Long id) throws EntityRetrievalException, AccessDeniedException;

    AnnouncementDTO getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException;

    List<AnnouncementDTO> getAllFuture();

    List<AnnouncementDTO> getAllCurrentAndFuture();
}
