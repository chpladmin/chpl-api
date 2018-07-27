package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.ProductActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.util.JSONUtils;

@Service
public class ActivityManagerImpl implements ActivityManager {
    private static final Logger LOGGER = LogManager.getLogger(ActivityManagerImpl.class);

    @Autowired private CertificationBodyManager acbManager;
    @Autowired private TestingLabManager atlManager;
    @Autowired private PendingCertifiedProductDAO pcpDao;
    @Autowired private UserDAO userDao;
    @Autowired
    ActivityDAO activityDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    DeveloperDAO devDao;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonFactory factory = jsonMapper.getFactory();

    @Override
    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (final IOException e) {

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
            if (Util.getCurrentUser() != null) {
                dto.setLastModifiedUser(Util.getCurrentUser().getId());
            }
            dto.setDeleted(false);

            activityDAO.create(dto);
        }

    }

    @Override
    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, String reason) throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        this.addActivity(concept, objectId, activityDescription, originalData, newData);
    }
    
    @Override
    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Long asUser)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (final IOException e) {

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

    @Override
    @Transactional
    public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, Object originalData,
            Object newData, Date timestamp)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        String originalDataStr = JSONUtils.toJSON(originalData);
        String newDataStr = JSONUtils.toJSON(newData);

        Boolean originalMatchesNew = false;

        try {
            originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
        } catch (final IOException e) {

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
            dto.setLastModifiedUser(Util.getCurrentUser().getId());
            dto.setDeleted(false);

            activityDAO.create(dto);
        }
    }

    @Override
    @Transactional
    public List<ActivityEvent> getAllActivityInDateRange(boolean showDeleted, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDTO> dtos = activityDAO.findAllInDateRange(showDeleted, startDate, endDate);
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : dtos) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    public List<ActivityEvent> getActivityForObject(ActivityConcept concept, Long objectId,
            Date startDate, Date endDate) throws JsonParseException, IOException {

        List<ActivityDTO> dtos = activityDAO.findByObjectId(false, objectId, concept, startDate, endDate);
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : dtos) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    public List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Date startDate,
            Date endDate) throws JsonParseException, IOException {

        List<ActivityDTO> dtos = activityDAO.findByConcept(false, concept, startDate, endDate);
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : dtos) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    /**
     * get all announcement activity; restricted to logged in users
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<ActivityEvent> getAnnouncementActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException {
        return getActivityForConcept(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, 
                startDate, endDate);
    }
    
    /**
     * get all activity for a specific announcement id; restricted to logged in users
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<ActivityEvent> getAnnouncementActivity(Long id, Date startDate,
            Date endDate) throws JsonParseException, IOException {
        return getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT, 
                id, startDate, endDate);
    }
    
    /**
     * Get activity only for public announcements.
     * This will return activity where the isPublic flag is true on both the
     * original and updated data.
     */
    @Override
    @Transactional
    public List<ActivityEvent> getPublicAnnouncementActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> dtos = activityDAO.findPublicAnnouncementActivity(startDate, endDate);
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : dtos) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }
    
    /**
     * Get activity only for a specific public announcement.
     * This will only return activity for the announcement if the isPublic flag
     * is set to true in both the original data and new data.
     */
    @Override
    @Transactional
    public List<ActivityEvent> getPublicAnnouncementActivity(Long id, Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> dtos = activityDAO.findPublicAnnouncementActivityById(id, startDate, endDate);
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : dtos) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ActivityEvent> getApiKeyActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException {
        return getActivityForConcept(ActivityConcept.ACTIVITY_CONCEPT_API_KEY, 
                startDate, endDate);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public List<ActivityEvent> getAcbActivity(boolean showDeleted, Date startDate,
            Date endDate) throws JsonParseException, IOException {
        
        List<ActivityDTO> acbActivity = null;
        if(Util.isUserRoleAdmin()) {
            acbActivity = activityDAO.findByConcept(
                showDeleted, ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, startDate, endDate);
        } else if(Util.isUserRoleAcbAdmin()){
            List<CertificationBodyDTO> allowedAcbs = acbManager.getAllForUser(showDeleted);
            acbActivity = activityDAO.findAcbActivity(allowedAcbs, startDate, endDate);
        }

        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : acbActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ACB') and "
            + "hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
            + "hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public List<ActivityEvent> getAcbActivity(boolean showDeleted, Long acbId, Date startDate,
            Date endDate) throws JsonParseException, IOException, EntityRetrievalException {
        List<ActivityDTO> acbActivity = null;
        if(Util.isUserRoleAdmin()) {
            acbActivity = activityDAO.findByObjectId(
                showDeleted, acbId, ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY, startDate, endDate);
        } else if(Util.isUserRoleAcbAdmin()) {
            CertificationBodyDTO foundAcb = acbManager.getById(acbId);
            List<CertificationBodyDTO> acbToQuery = new ArrayList<CertificationBodyDTO>(1);
            acbToQuery.add(foundAcb);
            acbActivity = activityDAO.findAcbActivity(acbToQuery, startDate, endDate);
        }
        
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();

        for (ActivityDTO dto : acbActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ATL')")
    public List<ActivityEvent> getAtlActivity(boolean showDeleted, Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> atlActivity = null;
        if(Util.isUserRoleAdmin()) {
            atlActivity = activityDAO.findByConcept(
                showDeleted, ActivityConcept.ACTIVITY_CONCEPT_ATL, startDate, endDate);
        } else if(Util.isUserRoleAtlAdmin()){
            List<TestingLabDTO> allowedAtls = atlManager.getAllForUser(showDeleted);
            atlActivity = activityDAO.findAtlActivity(allowedAtls, startDate, endDate);
        }
        
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : atlActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or "
            + "(hasRole('ROLE_ATL') and "
            + "hasPermission(#atlId, 'gov.healthit.chpl.dto.TestingLabDTO', read) or "
            + "hasPermission(#atlId, 'gov.healthit.chpl.dto.TestingLabDTO', admin))")
    public List<ActivityEvent> getAtlActivity(boolean showDeleted, Long atlId, Date startDate,
            Date endDate) throws JsonParseException, IOException, EntityRetrievalException {
        List<ActivityDTO> atlActivity = null;
        if(Util.isUserRoleAdmin()) {
            atlActivity = activityDAO.findByObjectId(
                showDeleted, atlId, ActivityConcept.ACTIVITY_CONCEPT_ATL, startDate, endDate);
        } else if(Util.isUserRoleAtlAdmin()) {
            TestingLabDTO foundAtl = atlManager.getById(atlId);
            List<TestingLabDTO> altToQuery = new ArrayList<TestingLabDTO>(1);
            altToQuery.add(foundAtl);
            atlActivity = activityDAO.findAtlActivity(altToQuery, startDate, endDate);
        }
        
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : atlActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public List<ActivityEvent> getPendingListingActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> pendingListingActivity = null;
        if(Util.isUserRoleAdmin()) {
            pendingListingActivity = activityDAO.findByConcept(false,
                    ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, startDate, endDate);
        } else if(Util.isUserRoleAcbAdmin()) {
            List<CertificationBodyDTO> allowedAcbs = acbManager.getAllForUser(true);
            pendingListingActivity = activityDAO.findPendingListingActivity(allowedAcbs, startDate, endDate);
        }
        
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : pendingListingActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public List<ActivityEvent> getPendingListingActivity(Long pendingListingId, 
            Date startDate, Date endDate) throws JsonParseException, IOException, EntityRetrievalException {
        List<ActivityDTO> pendingListingActivity = null;
        if(Util.isUserRoleAdmin()) {
            pendingListingActivity = activityDAO.findByObjectId(false, pendingListingId,
                    ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, 
                    startDate, endDate);
        } else if(Util.isUserRoleAcbAdmin()) {
            PendingCertifiedProductDTO pendingListing = pcpDao.findById(pendingListingId, true);
            if(pendingListing != null) {
                //try to get the listing's acb to make sure this user has access to it
                CertificationBodyDTO foundAcb = acbManager.getById(pendingListing.getCertificationBodyId());
                if(foundAcb != null) {
                    //get activity for this pending listing
                    pendingListingActivity = activityDAO.findPendingListingActivity(
                            pendingListing.getId(), startDate, endDate);
                }
            }
        }
        
        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : pendingListingActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB', 'ROLE_ATL', 'ROLE_CMS_STAFF')")
    public List<ActivityEvent> getUserActivity(Date startDate,
            Date endDate) throws JsonParseException, IOException {
        List<ActivityDTO> userActivity = null;
        if(Util.isUserRoleAdmin()) {
            userActivity = activityDAO.findByConcept(false, 
                    ActivityConcept.ACTIVITY_CONCEPT_USER, startDate, endDate);
        } else {
            Collection<Long> allowedUserIds = getAllowedUsersForActivitySearch();
            userActivity = activityDAO.findUserActivity(allowedUserIds, startDate, endDate);
        }

        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        for (ActivityDTO dto : userActivity) {
            ActivityEvent event = getActivityEventFromDTO(dto);
            events.add(event);
        }
        return events;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB', 'ROLE_ATL', 'ROLE_CMS_STAFF')")
    public List<ActivityEvent> getUserActivity(Long userId, 
            Date startDate, Date endDate) throws JsonParseException, IOException {
        List<ActivityEvent> userActivity = null;
        if(Util.isUserRoleAdmin()) {
            userActivity = getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_USER, 
                userId, startDate, endDate);
        } else {
            //make sure this user has permission to get activity for this user id
            Collection<Long> allowedUserIds = getAllowedUsersForActivitySearch();
            if(allowedUserIds.contains(userId)) {
                userActivity = getActivityForObject(ActivityConcept.ACTIVITY_CONCEPT_USER, 
                        userId, startDate, endDate);
            } else {
                throw new AccessDeniedException(null);
            }
        }
        
        return userActivity;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteActivity(Long toDelete) throws EntityRetrievalException {

        ActivityDTO dto = activityDAO.getById(toDelete);
        dto.setDeleted(true);
        activityDAO.update(dto);

    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    public List<UserActivity> getActivityByUser() throws JsonParseException, IOException, UserRetrievalException {

        Map<Long, List<ActivityDTO>> activity = activityDAO.findAllByUser();
        List<UserActivity> userActivities = new ArrayList<UserActivity>();

        for (Map.Entry<Long, List<ActivityDTO>> userEntry : activity.entrySet()) {
            UserDTO activityUser = userEntry.getValue().get(0).getUser();
            if (activityUser != null) {
                User userObj = new User(activityUser);

                List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();

                for (ActivityDTO userEventDTO : userEntry.getValue()) {
                    ActivityEvent event = getActivityEventFromDTO(userEventDTO);
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

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    public List<UserActivity> getActivityByUserInDateRange(Date startDate, Date endDate)
            throws JsonParseException, IOException, UserRetrievalException {

        Map<Long, List<ActivityDTO>> activity = activityDAO.findAllByUserInDateRange(startDate, endDate);
        List<UserActivity> userActivities = new ArrayList<UserActivity>();

        for (Map.Entry<Long, List<ActivityDTO>> userEntry : activity.entrySet()) {
            UserDTO activityUser = userEntry.getValue().get(0).getUser();
            if (activityUser != null) {
                User userObj = new User(activityUser);

                List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();

                for (ActivityDTO userEventDTO : userEntry.getValue()) {
                    ActivityEvent event = getActivityEventFromDTO(userEventDTO);
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

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    public List<ActivityEvent> getActivityForUser(Long userId) throws JsonParseException, IOException {

        List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();

        for (ActivityDTO userEventDTO : activityDAO.findByUserId(userId)) {
            ActivityEvent event = getActivityEventFromDTO(userEventDTO);
            userActivityEvents.add(event);
        }
        return userActivityEvents;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    public List<ActivityEvent> getActivityForUserInDateRange(Long userId, Date startDate, Date endDate)
            throws JsonParseException, IOException {

        List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();

        for (ActivityDTO userEventDTO : activityDAO.findByUserId(userId, startDate, endDate)) {
            ActivityEvent event = getActivityEventFromDTO(userEventDTO);
            userActivityEvents.add(event);
        }
        return userActivityEvents;
    }

    private Collection<Long> getAllowedUsersForActivitySearch() {
        Set<Long> allowedUserIds = new HashSet<Long>();
        //user can see their own activity
        allowedUserIds.add(Util.getCurrentUser().getId());
        
        //user can see activity for other users in the same acb
        if(Util.isUserRoleAcbAdmin()) {
            List<CertificationBodyDTO> allowedAcbs = acbManager.getAllForUser(false);
            for(CertificationBodyDTO acb : allowedAcbs) {
                List<UserDTO> acbUsers = acbManager.getAllUsersOnAcb(acb);
                for(UserDTO user : acbUsers) {
                    allowedUserIds.add(user.getId());
                }
            }
        }
        //user can see activity for other users in the same atl
        if(Util.isUserRoleAtlAdmin()) {
            List<TestingLabDTO> allowedAtls = atlManager.getAllForUser(false);
            for(TestingLabDTO atl : allowedAtls) {
                List<UserDTO> atlUsers = atlManager.getAllUsersOnAtl(atl);
                for(UserDTO user : atlUsers) {
                    allowedUserIds.add(user.getId());
                }
            }
        }
        //user can see activity for other users with role cms_staff
        if(Util.isUserRoleCmsStaff()) {
            List<UserDTO> cmsStaffUsers = userDao.getUsersWithPermission("ROLE_CMS_STAFF");
            for(UserDTO user : cmsStaffUsers) {
                allowedUserIds.add(user.getId());
            }
        }
        return allowedUserIds;
    }
    
    private ActivityEvent getActivityEventFromDTO(ActivityDTO dto) throws JsonParseException, IOException {
        ActivityEvent event = null;
        if (dto.getConcept() == ActivityConcept.ACTIVITY_CONCEPT_PRODUCT) {
            event = new ProductActivityEvent();
        } else {
            event = new ActivityEvent();
        }

        event.setId(dto.getId());
        event.setDescription(dto.getDescription());
        event.setActivityDate(dto.getActivityDate());
        event.setActivityObjectId(dto.getActivityObjectId());
        event.setConcept(dto.getConcept());
        event.setResponsibleUser(dto.getUser() == null ? null : new User(dto.getUser()));

        JsonNode originalJSON = null;
        if (dto.getOriginalData() != null) {
            JsonParser origData = factory.createParser(dto.getOriginalData());
            originalJSON = jsonMapper.readTree(origData);
        }

        JsonNode newJSON = null;
        if (dto.getNewData() != null) {
            JsonParser newData = factory.createParser(dto.getNewData());
            newJSON = jsonMapper.readTree(newData);
        }

        event.setOriginalData(originalJSON);
        event.setNewData(newJSON);

        if (event instanceof ProductActivityEvent && event.getNewData() != null) {
            JsonNode devIdNode = event.getNewData().get("developerId");
            Long devId = devIdNode.asLong();
            if (devId != null) {
                try {
                    DeveloperDTO dev = devDao.getById(devId);
                    if (dev != null) {
                        ((ProductActivityEvent) event).setDeveloper(new Developer(dev));
                    }
                } catch (final EntityRetrievalException ex) {
                    LOGGER.error("Could not get developer with id " + devId);
                }
            }
        }
        return event;
    }

}
