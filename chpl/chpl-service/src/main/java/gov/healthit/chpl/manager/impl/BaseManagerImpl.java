package gov.healthit.chpl.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.activity.ActivityConcept;
import gov.healthit.chpl.activity.ActivityEventEmitter;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;

public abstract class BaseManagerImpl implements ActivityEventEmitter {
	
	@Autowired
	protected ActivityManager activityManager;

	@Override
	public void emitActivityEvent(String eventDescription) throws EntityCreationException, EntityRetrievalException {
		activityManager.addActivity(getConcept(), getObjectId(), eventDescription);
	}

}
