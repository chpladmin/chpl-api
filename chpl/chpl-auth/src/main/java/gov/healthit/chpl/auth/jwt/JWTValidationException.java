package gov.healthit.chpl.auth.jwt;

public class JWTValidationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	public JWTValidationException() { super(); }
	public JWTValidationException(String message) { super(message); }
	public JWTValidationException(String message, Throwable cause) { super(message, cause); }
	public JWTValidationException(Throwable cause) { super(cause); }
	
}

