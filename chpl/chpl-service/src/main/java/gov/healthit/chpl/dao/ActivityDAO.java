package gov.healthit.chpl.dao;


import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;

public interface ActivityDAO {
	
	public ActivityDTO create(ActivityDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ActivityDTO update(ActivityDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public ActivityDTO getById(Long id) throws EntityRetrievalException;
	public ActivityDTO getById(boolean showDeleted, Long id) throws EntityRetrievalException;
	public List<ActivityDTO> findAll(boolean showDeleted);
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept);
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept);
	public List<ActivityDTO> findAllInDateRange(boolean showDeleted, Date startDate, Date endDate);
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept, Date startDate, Date endDate);
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept, Date startDate, Date endDate);
	public List<ActivityDTO> findByUserId(Long userId, Date startDate, Date endDate);
	public List<ActivityDTO> findByUserId(Long userId);
	public Map<Long, List<ActivityDTO> > findAllByUser();
	public Map<Long, List<ActivityDTO>> findAllByUserInDateRange(Date startDate, Date endDate);

}
