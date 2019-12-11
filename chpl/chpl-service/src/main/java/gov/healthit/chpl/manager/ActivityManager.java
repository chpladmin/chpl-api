package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ProductActivityDetails;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.listener.QuestionableActivityListener;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.JSONUtils;

@Service("activityManager")
public class ActivityManager extends SecuredManager {
    private static Logger LOGGER = LogManager.getLogger(ActivityManager.class);

    private ActivityDAO activityDAO;
    private DeveloperDAO devDao;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonFactory factory = jsonMapper.getFactory();
    private QuestionableActivityListener questionableActivityListener;

    @Autowired
    public ActivityManager(ActivityDAO activityDAO, DeveloperDAO devDao,
            QuestionableActivityListener questionableActivityListener) {
        this.activityDAO = activityDAO;
        this.devDao = devDao;
        this.questionableActivityListener = questionableActivityListener;
    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId,
            String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (IOException e) {

        }

        // Do not add the activity if nothing has changed.
        if (!originalMatchesNew) {

            ActivityDTO dto = new ActivityDTO();
            dto.setConcept(concept);
            dto.setId(null);
            dto.setDescription(activityDescription);
            dto.setOriginalData(originalDataStr);
            dto.setNewData(newDataStr);
            dto.setActivityDate(new Date());
            dto.setActivityObjectId(objectId);
            dto.setCreationDate(new Date());
            dto.setLastModifiedDate(new Date());
            if (AuthUtil.getCurrentUser() != null) {
                dto.setLastModifiedUser(AuthUtil.getAuditId());
            }
            dto.setDeleted(false);

            activityDAO.create(dto);
        }

    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId,
            String activityDescription, Object originalData,
            Object newData, String reason)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        this.addActivity(concept, objectId, activityDescription, originalData, newData);
        questionableActivityListener.checkQuestionableActivity(concept, objectId, activityDescription, originalData, newData,
                reason);
    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId,
            String activityDescription, Object originalData,
            Object newData, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (IOException e) {

        }

        // Do not add the activity if nothing has changed.
        if (!originalMatchesNew) {

            ActivityDTO dto = new ActivityDTO();
            dto.setConcept(concept);
            dto.setId(null);
            dto.setDescription(activityDescription);
            dto.setOriginalData(originalDataStr);
            dto.setNewData(newDataStr);
            dto.setActivityDate(new Date());
            dto.setActivityObjectId(objectId);
            dto.setCreationDate(new Date());
            dto.setLastModifiedDate(new Date());
            dto.setLastModifiedUser(asUser);
            dto.setDeleted(false);

            activityDAO.create(dto);
        }

    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId,
            String activityDescription, Object originalData,
            Object newData, Date timestamp)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (IOException e) {

        }

        // Do not add the activity if nothing has changed.
        if (!originalMatchesNew) {

            ActivityDTO dto = new ActivityDTO();
            dto.setConcept(concept);
            dto.setId(null);
            dto.setDescription(activityDescription);
            dto.setOriginalData(JSONUtils.toJSON(originalData));
            dto.setNewData(JSONUtils.toJSON(newData));
            dto.setActivityDate(timestamp);
            dto.setActivityObjectId(objectId);
            dto.setCreationDate(new Date());
            dto.setLastModifiedDate(new Date());
            dto.setLastModifiedUser(AuthUtil.getAuditId());
            dto.setDeleted(false);

            activityDAO.create(dto);
        }
    }

    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ACTIVITY_DETAILS, returnObject)")
    @Transactional
    public ActivityDetails getActivityById(Long activityId)
            throws EntityRetrievalException, JsonParseException, IOException {
        ActivityDTO result = activityDAO.getById(activityId);
        ActivityDetails event = getActivityDetailsFromDTO(result);
        return event;
    }

    @Transactional
    public List<ActivityDetails> getActivityForObject(ActivityConcept concept, Long objectId, Date startDate,
            Date endDate) throws JsonParseException, IOException {

        List<ActivityDTO> dtos = activityDAO.findByObjectId(objectId, concept, startDate, endDate);
        List<ActivityDetails> events = new ArrayList<ActivityDetails>();

        for (ActivityDTO dto : dtos) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    public List<ActivityDetails> getActivityForConcept(ActivityConcept concept, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDTO> dtos = activityDAO.findByConcept(concept, startDate, endDate);
        List<ActivityDetails> events = new ArrayList<ActivityDetails>();

        for (ActivityDTO dto : dtos) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    /**
     * Get activity only for public announcements. This will return activity where the isPublic flag is true on both the
     * original and updated data.
     */
    @Transactional
    public List<ActivityDetails> getPublicAnnouncementActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> dtos = activityDAO.findPublicAnnouncementActivity(startDate, endDate);
        List<ActivityDetails> events = new ArrayList<ActivityDetails>();

        for (ActivityDTO dto : dtos) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    /**
     * Get activity only for a specific public announcement. This will only return activity for the announcement if the
     * isPublic flag is set to true in both the original data and new data.
     */
    @Transactional
    public List<ActivityDetails> getPublicAnnouncementActivity(Long id, Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> dtos = activityDAO.findPublicAnnouncementActivityById(id, startDate, endDate);
        List<ActivityDetails> events = new ArrayList<ActivityDetails>();

        for (ActivityDTO dto : dtos) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getApiKeyActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        return getActivityForConcept(ActivityConcept.API_KEY, startDate, endDate);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getAllAcbActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> acbActivity = activityDAO.findByConcept(ActivityConcept.CERTIFICATION_BODY,
                startDate, endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : acbActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_BY_ACB)")
    public List<ActivityDetails> getAcbActivity(List<CertificationBodyDTO> acbs, Date startDate,
            Date endDate) throws JsonParseException, IOException {

        List<ActivityDTO> acbActivity = activityDAO.findAcbActivity(acbs, startDate, endDate);
        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : acbActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getAllAtlActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> atlActivity = activityDAO.findByConcept(ActivityConcept.TESTING_LAB, startDate,
                endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : atlActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ATL')")
    public List<ActivityDetails> getAtlActivity(List<TestingLabDTO> atls, Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> atlActivity = activityDAO.findAtlActivity(atls, startDate, endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : atlActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getAllPendingListingActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<ActivityDTO> pendingListingActivity = activityDAO
                .findByConcept(ActivityConcept.PENDING_CERTIFIED_PRODUCT, startDate, endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : pendingListingActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_PENDING_LISTING_ACTIVITY_BY_ACB)")
    public List<ActivityDetails> getPendingListingActivityByAcb(List<CertificationBodyDTO> acbs, Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> pendingListingActivity = activityDAO.findPendingListingActivity(acbs, startDate, endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : pendingListingActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_PENDING_LISTING_ACTIVITY)")
    public List<ActivityDetails> getPendingListingActivity(Long pendingListingId, Date startDate, Date endDate)
            throws JsonParseException, IOException, EntityRetrievalException {
        List<ActivityDTO> pendingListingActivity = activityDAO.findPendingListingActivity(pendingListingId, startDate,
                endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : pendingListingActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getAllUserActivity(Date startDate, Date endDate)
            throws JsonParseException, IOException {
        return getActivityForConcept(ActivityConcept.USER, startDate, endDate);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_USER_ACTIVITY)")
    public List<ActivityDetails> getUserActivity(Set<Long> userIds, Date startDate, Date endDate)
            throws JsonParseException, IOException {
        List<Long> userIdList = new ArrayList<Long>();
        userIdList.addAll(userIds);
        List<ActivityDTO> userActivity = activityDAO.findUserActivity(userIdList, startDate, endDate);

        List<ActivityDetails> events = new ArrayList<ActivityDetails>();
        for (ActivityDTO dto : userActivity) {
            ActivityDetails event = getActivityDetailsFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<UserActivity> getActivityByUserInDateRange(Date startDate, Date endDate)
            throws JsonParseException, IOException, UserRetrievalException {

        Map<Long, List<ActivityDTO>> activity = activityDAO.findAllByUserInDateRange(startDate, endDate);
        List<UserActivity> userActivities = new ArrayList<UserActivity>();

        for (Map.Entry<Long, List<ActivityDTO>> userEntry : activity.entrySet()) {
            UserDTO activityUser = userEntry.getValue().get(0).getUser();
            if (activityUser != null) {
                User userObj = new User(activityUser);

                List<ActivityDetails> userActivityEvents = new ArrayList<ActivityDetails>();

                for (ActivityDTO userEventDTO : userEntry.getValue()) {
                    ActivityDetails event = getActivityDetailsFromDTO(userEventDTO);
                    userActivityEvents.add(event);
                }

                UserActivity userActivity = new UserActivity();
                userActivity.setUser(userObj);
                userActivity.setEvents(userActivityEvents);
                userActivities.add(userActivity);
            }
        }
        return userActivities;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ActivityDetails> getActivityForUserInDateRange(Long userId, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDetails> userActivityEvents = new ArrayList<ActivityDetails>();

        for (ActivityDTO userEventDTO : activityDAO.findByUserId(userId, startDate, endDate)) {
            ActivityDetails event = getActivityDetailsFromDTO(userEventDTO);
            userActivityEvents.add(event);
        }
        return userActivityEvents;
    }

    private ActivityDetails getActivityDetailsFromDTO(ActivityDTO dto) throws JsonParseException, IOException {
        ActivityDetails event = null;
        if (dto.getConcept() == ActivityConcept.PRODUCT) {
            event = new ProductActivityDetails();
        } else {
            event = new ActivityDetails();
        }

        event.setId(dto.getId());
        event.setDescription(dto.getDescription());
        event.setActivityDate(dto.getActivityDate());
        event.setActivityObjectId(dto.getActivityObjectId());
        event.setConcept(dto.getConcept());
        event.setResponsibleUser(dto.getUser() == null ? null : new User(dto.getUser()));

        JsonNode originalJSON = null;
        if (dto.getOriginalData() != null) {
            try (JsonParser origData = factory.createParser(dto.getOriginalData())) {
                originalJSON = jsonMapper.readTree(origData);
            }
        }

        JsonNode newJSON = null;
        if (dto.getNewData() != null) {
            try (JsonParser newData = factory.createParser(dto.getNewData())) {
                newJSON = jsonMapper.readTree(newData);

            }
        }

        event.setOriginalData(originalJSON);
        event.setNewData(newJSON);

        if (event instanceof ProductActivityDetails && event.getNewData() != null) {
            JsonNode devIdNode = event.getNewData().get("developerId");
            if (devIdNode != null) {
                Long devId = devIdNode.asLong();
                if (devId != null) {
                    try {
                        DeveloperDTO dev = devDao.getById(devId, true);
                        if (dev != null) {
                            ((ProductActivityDetails) event).setDeveloper(new Developer(dev));
                        }
                    } catch (EntityRetrievalException ex) {
                        LOGGER.error("Could not get developer with id " + devId);
                    }
                }
            }
        }
        return event;
    }

}
