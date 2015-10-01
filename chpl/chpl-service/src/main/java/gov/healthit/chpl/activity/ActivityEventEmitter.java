package gov.healthit.chpl.activity;

public interface ActivityEventEmitter {
	
	public String getConceptName();
	public Long getObjectId();
	public void emitActivityEvent(String eventDescription);
	
}
