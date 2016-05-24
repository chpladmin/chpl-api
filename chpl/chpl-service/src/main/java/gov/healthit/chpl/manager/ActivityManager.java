package gov.healthit.chpl.manager;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.dto.ActivityDTO;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ActivityManager {

	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, Long asUser) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, Date timestamp) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public List<ActivityEvent> getAllActivity(boolean showDeleted) throws JsonParseException, IOException;
	public List<ActivityEvent> getActivityForObject(boolean showDeleted, ActivityConcept concept, Long objectId) throws JsonParseException, IOException;
	public List<ActivityEvent> getActivityForConcept(boolean showDeleted, ActivityConcept concept) throws JsonParseException, IOException;
	public List<ActivityEvent> getAllActivityInLastNDays(boolean showDeleted, Integer lastNDays) throws JsonParseException, IOException;
	public List<ActivityEvent> getActivityForObject(boolean showDeleted, ActivityConcept concept, Long objectId, Integer lastNDays) throws JsonParseException, IOException;
	public List<ActivityEvent> getActivityForConcept(boolean showDeleted, ActivityConcept concept, Integer lastNDays) throws JsonParseException, IOException;
	public void deleteActivity(Long toDelete) throws EntityRetrievalException;
	public List<UserActivity> getActivityByUser() throws JsonParseException, IOException, UserRetrievalException;
	public List<UserActivity> getActivityByUserInLastNDays(Integer nDays) throws JsonParseException, IOException, UserRetrievalException;
	public List<ActivityEvent> getActivityForUser(Long userId) throws JsonParseException, IOException;
	public List<ActivityEvent> getActivityForUserInLastNDays(Long userId, Integer nDays) throws JsonParseException, IOException;
	
}