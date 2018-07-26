package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ActivityManager {

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, String reason) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Date timestamp)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    List<ActivityEvent> getAllActivityInDateRange(boolean showDeleted, Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAnnouncementActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAnnouncementActivity(Long id, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getPublicAnnouncementActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    public List<ActivityEvent> getPublicAnnouncementActivity(Long id,Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAcbActivity(boolean showDeleted, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAcbActivity(boolean showDeleted, Long acbId, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAtlActivity(boolean showDeleted, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getAtlActivity(boolean showDeleted, Long atlId, Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getPendingListingActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getPendingListingActivity(Long pendingListingId, 
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getUserActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;
    
    List<ActivityEvent> getUserActivity(Long userId, 
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getApiKeyActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    void deleteActivity(Long toDelete) throws EntityRetrievalException;

    List<UserActivity> getActivityByUser() throws JsonParseException, IOException, UserRetrievalException;

    List<UserActivity> getActivityByUserInDateRange(Date startDate, Date endDate)
            throws JsonParseException, IOException, UserRetrievalException;

    List<ActivityEvent> getActivityForUser(Long userId) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForUserInDateRange(Long userId, Date startDate, Date endDate)
            throws JsonParseException, IOException;

}
