package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ActivityManager {

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, String reason)
                    throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Date timestamp)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException;

    List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getPublicAnnouncementActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getPublicAnnouncementActivity(Long id, Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getAllAcbActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getAcbActivity(List<CertificationBodyDTO> acbs,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getAllAtlActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getAtlActivity(List<TestingLabDTO> atls, Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getAllPendingListingActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getPendingListingActivityByAcb(List<CertificationBodyDTO> acbs,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getPendingListingActivity(Long pendingListingId,
            Date startDate, Date endDate) throws JsonParseException, IOException, EntityRetrievalException;

    List<ActivityEvent> getAllUserActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException;

    List<ActivityEvent> getUserActivity(Set<Long> userIds,
            Date startDate, Date endDate) throws JsonParseException, IOException;

    List<ActivityEvent> getApiKeyActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException;

    List<UserActivity> getActivityByUserInDateRange(Date startDate, Date endDate)
            throws JsonParseException, IOException, UserRetrievalException;

    List<ActivityEvent> getActivityForUserInDateRange(Long userId, Date startDate, Date endDate)
            throws JsonParseException, IOException;

}
