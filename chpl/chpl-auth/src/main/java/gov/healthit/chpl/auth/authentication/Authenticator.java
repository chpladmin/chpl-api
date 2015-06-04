package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.user.User;


public interface Authenticator {

	public String getJWT(User user) throws JWTCreationException;
	public String getJWT(LoginCredentials credentials) throws JWTCreationException;
	
}