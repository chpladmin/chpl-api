package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityManager;










import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class ActivityManagerImpl implements ActivityManager {

	@Autowired
	ActivityDAO activityDAO;
	
	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId, String activityDescription, String originalData, String newData
			) throws EntityCreationException, EntityRetrievalException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setOriginalData(originalData);
		dto.setNewData(newData);
		dto.setActivityDate(new Date());
		dto.setActivityObjectId(objectId);
		dto.setCreationDate(new Date());
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setDeleted(false);
		
		activityDAO.create(dto);
		
	}

	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, String originalData, String newData, Date timestamp) throws EntityCreationException, EntityRetrievalException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setOriginalData(originalData);
		dto.setNewData(newData);
		dto.setActivityDate(timestamp);
		dto.setActivityObjectId(objectId);
		dto.setCreationDate(new Date());
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setDeleted(false);
		
		activityDAO.create(dto);
	}

	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setOriginalData(JSONUtils.toJSON(originalData));
		dto.setNewData(JSONUtils.toJSON(newData));
		dto.setActivityDate(new Date());
		dto.setActivityObjectId(objectId);
		dto.setCreationDate(new Date());
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setDeleted(false);
		
		activityDAO.create(dto);
		
	}

	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData,
			Date timestamp) throws EntityCreationException,
			EntityRetrievalException, JsonProcessingException {
		
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
	
	@Override
	@Transactional
	public List<ActivityEvent> getAllActivity() {
		List<ActivityDTO> dtos = activityDAO.findAll();
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(
			ActivityConcept concept, Long objectId) {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(objectId, concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept) {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}
	
	@Override
	@Transactional
	public List<ActivityEvent> getAllActivityInLastNDays(Integer lastNDays) {
		List<ActivityDTO> dtos = activityDAO.findAllInLastNDays(lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(
			ActivityConcept concept, Long objectId, Integer lastNDays) {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(objectId, concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Integer lastNDays) {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}

}