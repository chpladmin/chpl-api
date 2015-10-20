package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ActivityConceptDTO;

import java.util.List;

public interface ActivityConceptDAO {
	
	public ActivityConceptDTO create(ActivityConceptDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityConceptDTO update(ActivityConceptDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityConceptDTO getById(Long id) throws EntityRetrievalException;
	public List<ActivityConceptDTO> findAll();

}
