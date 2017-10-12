package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;

public interface ActivityManager {

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Date timestamp)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    List<ActivityEvent> getAllActivity(boolean showDeleted) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForObject(boolean showDeleted, ActivityConcept concept, Long objectId)
            throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForConcept(boolean showDeleted, ActivityConcept concept)
            throws JsonParseException, IOException;

    List<ActivityEvent> getAllActivityInDateRange(boolean showDeleted, Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForObject(boolean showDeleted, ActivityConcept concept, Long objectId,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForConcept(boolean showDeleted, ActivityConcept concept, Date startDate,
            Date endDate) throws JsonParseException, IOException;

    void deleteActivity(Long toDelete) throws EntityRetrievalException;

    List<UserActivity> getActivityByUser() throws JsonParseException, IOException, UserRetrievalException;

    List<UserActivity> getActivityByUserInDateRange(Date startDate, Date endDate)
            throws JsonParseException, IOException, UserRetrievalException;

    List<ActivityEvent> getActivityForUser(Long userId) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForUserInDateRange(Long userId, Date startDate, Date endDate)
            throws JsonParseException, IOException;

}
