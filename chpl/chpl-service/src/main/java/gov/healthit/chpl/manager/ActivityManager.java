package gov.healthit.chpl.manager;

import gov.healthit.chpl.activity.ActivityConcept;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;

import java.util.Date;
import java.util.List;

public interface ActivityManager {
	
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription) throws EntityCreationException, EntityRetrievalException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Date timestamp) throws EntityCreationException, EntityRetrievalException;
	public List<ActivityEvent> getAllActivity();
	public List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId);
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept);
}