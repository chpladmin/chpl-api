package gov.healthit.chpl.dao;


import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.List;

public interface ActivityDAO {
	
	public ActivityDTO create(ActivityDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityDTO update(ActivityDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityDTO getById(Long id) throws EntityRetrievalException;
	public List<ActivityDTO> findAll();
	public List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept);
	public List<ActivityDTO> findByConcept(ActivityConcept concept);

}
