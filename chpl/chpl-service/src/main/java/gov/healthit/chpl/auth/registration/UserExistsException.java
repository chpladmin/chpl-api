package gov.healthit.chpl.auth.registration;

public class UserExistsException extends Exception {

	private static final long serialVersionUID = 1L;
	public UserExistsException() { super(); }
	public UserExistsException(String message) { super(message); }
	public UserExistsException(String message, Throwable cause) { super(message, cause); }
	public UserExistsException(Throwable cause) { super(cause); }
	
}
