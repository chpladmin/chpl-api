package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;

public interface DeveloperManager {
    public List<DeveloperDTO> getAll();

    public List<DeveloperDTO> getAllIncludingDeleted();

    public DeveloperDTO getById(Long id) throws EntityRetrievalException;

    public List<DeveloperTransparency> getDeveloperCollection();

    public DeveloperDTO update(DeveloperDTO developer)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    public DeveloperDTO create(DeveloperDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    public DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    public DecertifiedDeveloperResults getDecertifiedDevelopers() throws EntityRetrievalException;
}
