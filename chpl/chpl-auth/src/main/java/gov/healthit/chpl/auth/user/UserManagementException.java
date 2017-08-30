package gov.healthit.chpl.auth.user;

public class UserManagementException extends Exception {

	private static final long serialVersionUID = 1L;
	public UserManagementException() { super(); }
	public UserManagementException(String message) { super(message); }
	public UserManagementException(String message, Throwable cause) { super(message, cause); }
	public UserManagementException(Throwable cause) { super(cause); }
	
}
