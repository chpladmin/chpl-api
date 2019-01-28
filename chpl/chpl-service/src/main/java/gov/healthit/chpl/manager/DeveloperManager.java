package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;

public interface DeveloperManager {
    List<DeveloperDTO> getAll();

    List<DeveloperDTO> getAllIncludingDeleted();

    DeveloperDTO getById(Long id) throws EntityRetrievalException;

    List<DeveloperTransparency> getDeveloperCollection();

    DeveloperDTO update(DeveloperDTO developer)
            throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, MissingReasonException;

    DeveloperDTO create(DeveloperDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException;

    List<DecertifiedDeveloperResult> getDecertifiedDevelopers() throws EntityRetrievalException;
}
