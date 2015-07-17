package gov.healthit.chpl.auth.authentication;

import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserDTO;


public interface Authenticator {

	public String getJWT(UserDTO user) throws JWTCreationException;
	public String getJWT(LoginCredentials credentials) throws JWTCreationException;
	
}