package gov.healthit.chpl.activity;

import gov.healthit.chpl.domain.ActivityEvent;

import java.util.Date;
import java.util.List;

public interface ActivityManager {
	
	public void addActivity(String className, Long id, String activityDescription);
	public void addActivity(String className, Long id, String activityDescription, Date timestamp);
	public List<ActivityEvent> getAllActivity();
	public List<ActivityEvent> getActivityForObject(ActivityEventEmitter eventEmitter);
	public List<ActivityEvent> getActivityForObject(String className, Long id);
	
}