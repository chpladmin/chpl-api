package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityManager;


























import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ActivityManagerImpl implements ActivityManager {

	@Autowired
	ActivityDAO activityDAO;
	
	@Autowired
	UserDAO userDAO;
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	private JsonFactory factory = jsonMapper.getFactory();
	
	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		String originalDataStr = JSONUtils.toJSON(originalData);
		String newDataStr = JSONUtils.toJSON(newData);
		
		Boolean originalMatchesNew = false;
		
		try {
			originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
		} catch (IOException e){
			
		}
		
		
		// Do not add the activity if nothing has changed.
		if (!originalMatchesNew){
			
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
			if(Util.getCurrentUser() != null) {
				dto.setLastModifiedUser(Util.getCurrentUser().getId());
			}
			dto.setDeleted(false);
			
			activityDAO.create(dto);
		}
		
	}
	
	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData, Long asUser)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		String originalDataStr = JSONUtils.toJSON(originalData);
		String newDataStr = JSONUtils.toJSON(newData);
		
		Boolean originalMatchesNew = false;
		
		try {
			originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
		} catch (IOException e){
			
		}
		
		
		// Do not add the activity if nothing has changed.
		if (!originalMatchesNew){
			
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
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData,
			Date timestamp) throws EntityCreationException,
			EntityRetrievalException, JsonProcessingException {
		
		String originalDataStr = JSONUtils.toJSON(originalData);
		String newDataStr = JSONUtils.toJSON(newData);
		
		Boolean originalMatchesNew = false;
		
		try {
			originalMatchesNew = JSONUtils.jsonEquals(originalDataStr, newDataStr);
		} catch (IOException e){
			
		}
		
		// Do not add the activity if nothing has changed.
		if (!originalMatchesNew){
		
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
	public List<ActivityEvent> getAllActivity(boolean showDeleted) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findAll(showDeleted);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(boolean showDeleted,
			ActivityConcept concept, Long objectId) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(showDeleted, objectId, concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}


	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(boolean showDeleted, ActivityConcept concept) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(showDeleted, concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}
	
	@Override
	@Transactional
	public List<ActivityEvent> getAllActivityInLastNDays(boolean showDeleted, Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findAllInLastNDays(showDeleted, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(boolean showDeleted,
			ActivityConcept concept, Long objectId, Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(showDeleted, objectId, concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(boolean showDeleted,
			ActivityConcept concept, Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(showDeleted, concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteActivity(Long toDelete) throws EntityRetrievalException{
		
		ActivityDTO dto = activityDAO.getById(toDelete);
		dto.setDeleted(true);
		activityDAO.update(dto);
		
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<UserActivity> getActivityByUser() throws JsonParseException, IOException, UserRetrievalException{
		
		Map<Long, List<ActivityDTO> > activity = activityDAO.findAllByUser();
		List<UserActivity> userActivities = new ArrayList<UserActivity>();
		
		for (Map.Entry<Long, List<ActivityDTO> > userEntry : activity.entrySet()){
			
			UserDTO userDto = userDAO.getById(userEntry.getKey());
			User userObj = new User(userDto);
			
			List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();
			
			for (ActivityDTO userEventDTO : userEntry.getValue()){
				ActivityEvent event = getActivityEventFromDTO(userEventDTO);
				userActivityEvents.add(event);
			}
			
			UserActivity userActivity = new UserActivity();
			userActivity.setUser(userObj);
			userActivity.setEvents(userActivityEvents);
			userActivities.add(userActivity);
		}
		return userActivities;
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<UserActivity> getActivityByUserInLastNDays(Integer nDays) throws JsonParseException, IOException, UserRetrievalException{
		
		Map<Long, List<ActivityDTO> > activity = activityDAO.findAllByUserInLastNDays(nDays);
		List<UserActivity> userActivities = new ArrayList<UserActivity>();
		
		
		for (Map.Entry<Long, List<ActivityDTO> > userEntry : activity.entrySet()){
			
			UserDTO userDto = userDAO.getById(userEntry.getKey());
			User userObj = new User(userDto);
			
			List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();
			
			for (ActivityDTO userEventDTO : userEntry.getValue()){
				ActivityEvent event = getActivityEventFromDTO(userEventDTO);
				userActivityEvents.add(event);
			}
			
			UserActivity userActivity = new UserActivity();
			userActivity.setUser(userObj);
			userActivity.setEvents(userActivityEvents);
			userActivities.add(userActivity);
		}
		return userActivities;
	}
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ActivityEvent> getActivityForUser(Long userId) throws JsonParseException, IOException{
		
		List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO userEventDTO : activityDAO.findByUserId(userId)){
			ActivityEvent event = getActivityEventFromDTO(userEventDTO);
			userActivityEvents.add(event);
		}
		return userActivityEvents;
	}
	
	
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<ActivityEvent> getActivityForUserInLastNDays(Long userId, Integer nDays) throws JsonParseException, IOException{
		
		List<ActivityEvent> userActivityEvents = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO userEventDTO : activityDAO.findByUserId(userId, nDays)){
			ActivityEvent event = getActivityEventFromDTO(userEventDTO);
			userActivityEvents.add(event);
		}
		return userActivityEvents;
	}
	

	private ActivityEvent getActivityEventFromDTO(ActivityDTO dto) throws JsonParseException, IOException{
		
		ActivityEvent event = new ActivityEvent();
		
		event.setId(dto.getId());
		event.setDescription(dto.getDescription());
		event.setActivityDate(dto.getActivityDate());
		event.setActivityObjectId(dto.getActivityObjectId());
		event.setConcept(dto.getConcept());			
		
		JsonNode originalJSON = null;
		if (dto.getOriginalData()!= null){
			JsonParser origData = factory.createParser(dto.getOriginalData());
			originalJSON = jsonMapper.readTree(origData);
		}
		
		JsonNode newJSON = null;
		if (dto.getNewData()!= null){
			JsonParser newData = factory.createParser(dto.getNewData());
			newJSON = jsonMapper.readTree(newData);
		}
		
		event.setOriginalData(originalJSON);
		event.setNewData(newJSON);
		
		return event;
	}

}