package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ActivityClassDTO;

import java.util.List;

public interface ActivityClassDAO {
	
	public ActivityClassDTO create(ActivityClassDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityClassDTO update(ActivityClassDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityClassDTO getById(Long id) throws EntityRetrievalException;
	public List<ActivityClassDTO> findAll();

}
