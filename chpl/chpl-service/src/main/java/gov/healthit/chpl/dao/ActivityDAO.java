package gov.healthit.chpl.dao;


import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

import java.util.List;
import java.util.Map;

public interface ActivityDAO {
	
	public ActivityDTO create(ActivityDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityDTO update(ActivityDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityDTO getById(Long id) throws EntityRetrievalException;
	public ActivityDTO getById(boolean showDeleted, Long id) throws EntityRetrievalException;
	public List<ActivityDTO> findAll(boolean showDeleted);
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept);
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept);
	public List<ActivityDTO> findAllInLastNDays(boolean showDeleted, Integer lastNDays);
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept, Integer lastNDays);
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept, Integer lastNDays);
	public List<ActivityDTO> findByUserId(Long userId, Integer lastNDays);
	public List<ActivityDTO> findByUserId(Long userId);
	public Map<Long, List<ActivityDTO> > findAllByUser();
	public Map<Long, List<ActivityDTO>> findAllByUserInLastNDays(Integer lastNDays);

}
