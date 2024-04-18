package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ProductActivityDetails;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.listener.ChplProductNumberChangedListener;
import gov.healthit.chpl.listener.QuestionableActivityListener;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.subscription.SubscriptionObserver;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
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
    private SubscriptionObserver subscriptionObserver;
    private CognitoApiWrapper cognitoApiWrapper;
    private UserDAO userDAO;

    @Autowired
    public ActivityManager(ActivityDAO activityDAO, DeveloperDAO devDao,
            QuestionableActivityListener questionableActivityListener,
            ChplProductNumberChangedListener chplProductNumberChangedListener,
            SubscriptionObserver subscriptionObserver,
            CognitoApiWrapper cognitoApiWrapper,
            UserDAO userDAO) {
        this.activityDAO = activityDAO;
        this.devDao = devDao;
        this.questionableActivityListener = questionableActivityListener;
        this.chplProductNumberChangedListener = chplProductNumberChangedListener;
        this.subscriptionObserver = subscriptionObserver;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.userDAO = userDAO;
    }

    @Transactional
    public Long addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData)
            throws ActivityException {

        try {
            Date activityDate = new Date();
            ActivityDTO activity = addActivity(concept, objectId, activityDescription, originalData, newData, null, activityDate, getCurrentUser());
            if (activity != null) {
                questionableActivityListener.checkQuestionableActivity(activity, originalData, newData);
                chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData, activityDate);
                subscriptionObserver.checkActivityForSubscriptions(activity, originalData, newData);
            }
            return activity == null ? null : activity.getId();
        } catch (Exception e) {
            LOGGER.error("Error adding activity.", e);
            throw new ActivityException(e);
        }
    }

    @Transactional
    public Long addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, String reason)
            throws ActivityException {

        try {
            Date activityDate = new Date();
            ActivityDTO activity = addActivity(concept, objectId, activityDescription, originalData, newData, reason, activityDate, getCurrentUser());
            if (activity != null) {
                questionableActivityListener.checkQuestionableActivity(activity, originalData, newData, reason);
                chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData, activityDate);
                subscriptionObserver.checkActivityForSubscriptions(activity, originalData, newData);
            }
            return activity == null ? null : activity.getId();
        } catch (Exception e) {
            LOGGER.error("Error adding activity.", e);
            throw new ActivityException(e);
        }
    }

    @Transactional
    public Long addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData, Object newData, User asUser)
            throws ActivityException {

        try {
            Date activityDate = new Date();
            ActivityDTO activity = addActivity(concept, objectId, activityDescription, originalData, newData, null, activityDate, asUser);
            if (activity != null) {
                questionableActivityListener.checkQuestionableActivity(activity, originalData, newData);
                chplProductNumberChangedListener.recordChplProductNumberChanged(concept, objectId, originalData, newData, activityDate);
                subscriptionObserver.checkActivityForSubscriptions(activity, originalData, newData);
            }
            return activity == null ? null : activity.getId();
        } catch (Exception e) {
            LOGGER.error("Error adding activity.", e);
            throw new ActivityException(e);
        }
    }

    private User getCurrentUser() throws UserRetrievalException {
        User currentUser = null;
        if (AuthUtil.getCurrentUser().getAuthenticationSystem().equals(AuthenticationSystem.CHPL)) {
            currentUser = userDAO.getById(AuthUtil.getAuditId()).toDomain();
        } else if (AuthUtil.getCurrentUser().getAuthenticationSystem().equals(AuthenticationSystem.COGNTIO)) {
            currentUser = cognitoApiWrapper.getUserInfo(AuthUtil.getCurrentUser().getCognitoId());
        }
        return currentUser;
    }

    private ActivityDTO addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, String reason, Date timestamp, User asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = null;
        String newDataStr = null;

        if (concept.equals(ActivityConcept.CERTIFIED_PRODUCT)) {
            originalDataStr = JSONUtils.toJSONExcludingIgnoredFields(originalData);
            newDataStr = JSONUtils.toJSONExcludingIgnoredFields(newData);
        } else {
            originalDataStr = JSONUtils.toJSON(originalData);
            newDataStr = JSONUtils.toJSON(newData);
        }
        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        // Do not add the activity if nothing has changed.
        if (!originalMatchesNew) {
            ActivityDTO dto = new ActivityDTO();
            dto.setConcept(concept);
            dto.setId(null);
            dto.setDescription(activityDescription);
            dto.setOriginalData(originalDataStr);
            dto.setNewData(newDataStr);
            dto.setActivityDate(timestamp);
            dto.setActivityObjectId(objectId);
            dto.setReason(reason);
            dto.setCreationDate(new Date());
            dto.setLastModifiedDate(new Date());

            dto.setLastModifiedUser(asUser.getUserId());
            dto.setLastModifiedSsoUser(asUser.getCognitoId());
            dto.setUser(asUser);

            dto.setDeleted(false);
            Long activityId = activityDAO.create(dto);
            dto.setId(activityId);
            return dto;
        }
        return null;
    }

    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ACTIVITY_DETAILS, returnObject)")
    @Transactional
    public ActivityDetails getActivityById(Long activityId)
            throws EntityRetrievalException, JsonParseException, IOException, UserRetrievalException {
        ActivityDTO result = activityDAO.getById(activityId);
        ActivityDetails event = getActivityDetailsFromDTO(result);
        return event;
    }

    private ActivityDetails getActivityDetailsFromDTO(ActivityDTO dto) throws UserRetrievalException, JsonParseException, IOException {
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
        event.setResponsibleUser(dto.getUser());

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
