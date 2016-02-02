package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.DeveloperDTO;

public interface DeveloperManager {
	public List<DeveloperDTO> getAll();
	public DeveloperDTO getById(Long id) throws EntityRetrievalException;
	public DeveloperDTO update(DeveloperDTO developer) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public DeveloperDTO create(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void delete(DeveloperDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void delete(Long developerId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
}
