package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ActivityConceptDTO;

public interface ActivityConceptDAO {
	
	public ActivityConceptDTO create(ActivityConceptDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityConceptDTO update(ActivityConceptDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityConceptDTO getById(Long id) throws EntityRetrievalException;
	public List<ActivityConceptDTO> findAll();

}
