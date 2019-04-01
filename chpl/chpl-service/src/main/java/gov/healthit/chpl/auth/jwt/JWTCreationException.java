package gov.healthit.chpl.auth.jwt;

public class JWTCreationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	public JWTCreationException() { super(); }
	public JWTCreationException(String message) { super(message); }
	public JWTCreationException(String message, Throwable cause) { super(message, cause); }
	public JWTCreationException(Throwable cause) { super(cause); }
	
}

