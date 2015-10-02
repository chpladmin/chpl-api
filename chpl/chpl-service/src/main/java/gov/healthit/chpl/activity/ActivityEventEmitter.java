package gov.healthit.chpl.activity;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;

public interface ActivityEventEmitter {
	
	public ActivityConcept getConcept();
	public Long getObjectId();
	public void emitActivityEvent(String eventDescription) throws EntityCreationException, EntityRetrievalException;
	
}
