package gov.healthit.chpl.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.activity.ActivityEventEmitter;
import gov.healthit.chpl.activity.ActivityManager;

public abstract class BaseManagerImpl implements ActivityEventEmitter {
	
	@Autowired
	protected ActivityManager activityManager;
	
	protected String className;
	
	

}
