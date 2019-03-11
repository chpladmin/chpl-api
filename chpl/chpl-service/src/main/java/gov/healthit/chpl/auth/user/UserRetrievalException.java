package gov.healthit.chpl.auth.user;

public class UserRetrievalException extends Exception {

	private static final long serialVersionUID = 1L;
	public UserRetrievalException() { super(); }
	public UserRetrievalException(String message) { super(message); }
	public UserRetrievalException(String message, Throwable cause) { super(message, cause); }
	public UserRetrievalException(Throwable cause) { super(cause); }
	
}
