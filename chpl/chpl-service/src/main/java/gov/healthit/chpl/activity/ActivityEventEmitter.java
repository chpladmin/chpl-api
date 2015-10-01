package gov.healthit.chpl.activity;

public interface ActivityEventEmitter {
	
	public String getClassName();
	public Long getObjectId();
	public void emitActivityEvent(String eventDescription);
	
}
