package gov.healthit.chpl.activity;

import gov.healthit.chpl.domain.ActivityEvent;

import java.util.Date;
import java.util.List;

public class ActivityManagerImpl implements ActivityManager {

	@Override
	public void addActivity(String conceptName, Long id,
			String activityDescription) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addActivity(String conceptName, Long id,
			String activityDescription, Date timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ActivityEvent> getAllActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActivityEvent> getActivityForObject(
			ActivityEventEmitter eventEmitter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActivityEvent> getActivityForObject(String className, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
