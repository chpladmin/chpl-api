package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ProductActivityDetails;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listener.ChplProductNumberChangedListener;
import gov.healthit.chpl.listener.QuestionableActivityListener;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@Service("activityManager")
@Log4j2
public class ActivityManager extends SecuredManager {
    private ActivityDAO activityDAO;
    private DeveloperDAO devDao;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonFactory factory = jsonMapper.getFactory();
    private QuestionableActivityListener questionableActivityListener;
    private ChplProductNumberChangedListener chplProductNumberChangedListener;

    @Autowired
    public ActivityManager(ActivityDAO activityDAO, DeveloperDAO devDao,
            QuestionableActivityListener questionableActivityListener,
            ChplProductNumberChangedListener chplProductNumberChangedListener) {
        this.activityDAO = activityDAO;
        this.devDao = devDao;
        this.questionableActivityListener = questionableActivityListener;
        this.chplProductNumberChangedListener = chplProductNumberChangedListener;
    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        Long asUser = null;
        if (AuthUtil.getCurrentUser() != null) {
            asUser = AuthUtil.getAuditId();
        }

        addActivity(concept, objectId, activityDescription, originalData, newData, new Date(), asUser);
        questionableActivityListener.checkQuestionableActivity(concept, objectId, activityDescription, originalData, newData);
        chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData);
    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, String reason) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        Long asUser = null;
        if (AuthUtil.getCurrentUser() != null) {
            asUser = AuthUtil.getAuditId();
        }

        addActivity(concept, objectId, activityDescription, originalData, newData, new Date(), asUser);
        questionableActivityListener.checkQuestionableActivity(concept, objectId, activityDescription, originalData, newData, reason);
        chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData);
    }

    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Long asUser) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        addActivity(concept, objectId, activityDescription, originalData, newData, new Date(), asUser);
        questionableActivityListener.checkQuestionableActivity(concept, objectId, activityDescription, originalData, newData);
        chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData);
    }

    private void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Date timestamp, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);
        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return;
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
            dto.setLastModifiedUser(asUser);
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
                        Developer dev = devDao.getById(devId, true);
                        if (dev != null) {
                            ((ProductActivityDetails) event).setDeveloper(dev);
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
