package gov.healthit.chpl.dao;

public class EntityCreationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	public EntityCreationException() { super(); }
	public EntityCreationException(String message) { super(message); }
	public EntityCreationException(String message, Throwable cause) { super(message, cause); }
	public EntityCreationException(Throwable cause) { super(cause); }

}
