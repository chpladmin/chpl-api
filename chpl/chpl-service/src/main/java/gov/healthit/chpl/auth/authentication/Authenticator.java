package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.User;



public interface Authenticator {

	public User getUser(LoginCredentials credentials) throws BadCredentialsException;
	public String getJWT(User user) throws JWTCreationException;
	public String getJWT(LoginCredentials credentials) throws JWTCreationException;
}
