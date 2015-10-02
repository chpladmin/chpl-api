package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.activity.ActivityConcept;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityManagerImpl implements ActivityManager {

	@Autowired
	ActivityDAO activityDAO;
	
	@Override
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription) throws EntityCreationException, EntityRetrievalException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setActivityDate(new Date());
		dto.setActivityObjectId(objectId);
		
		activityDAO.create(dto);
		
	}

	@Override
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Date timestamp) throws EntityCreationException, EntityRetrievalException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setActivityDate(timestamp);
		dto.setActivityObjectId(objectId);
		
		activityDAO.create(dto);
		
	}

	@Override
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
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept) {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = new ActivityEvent(dto);
			events.add(event);
		}
		return events;
	}
	
}
