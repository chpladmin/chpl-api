package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ActivityManager {
	
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, String originalData, String newData) throws EntityCreationException, EntityRetrievalException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, String originalData, String newData, Date timestamp) throws EntityCreationException, EntityRetrievalException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, Date timestamp) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public List<ActivityEvent> getAllActivity();
	public List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId);
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept);
	
}