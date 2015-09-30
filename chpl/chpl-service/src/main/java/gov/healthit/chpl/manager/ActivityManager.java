package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.ActivityEvent;

import java.util.Date;
import java.util.List;

public interface ActivityManager {
	
	public void addActivity(Object object, Long id, String activityDescription);
	public void addActivity(Object object, Long id, String activityDescription, Date timestamp);
	public void addActivity(String classIdentifier, Long id, String activityDescription);
	public void addActivity(String classIdentifier, Long id, String activityDescription, Date timestamp);
	public List<ActivityEvent> getAllActivity();
	public List<ActivityEvent> getActivityForObject(Object object);
	
}
