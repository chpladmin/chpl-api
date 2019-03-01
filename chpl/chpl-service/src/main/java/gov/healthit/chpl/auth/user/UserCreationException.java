package gov.healthit.chpl.auth.user;

public class UserCreationException extends Exception {

	private static final long serialVersionUID = 1L;
	public UserCreationException() { super(); }
	public UserCreationException(String message) { super(message); }
	public UserCreationException(String message, Throwable cause) { super(message, cause); }
	public UserCreationException(Throwable cause) { super(cause); }
	
}
