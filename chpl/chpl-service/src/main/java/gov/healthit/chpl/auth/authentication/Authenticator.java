package gov.healthit.chpl.auth.authentication;

import org.springframework.security.authentication.AccountStatusException;

import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;



public interface Authenticator {

	public String getJWT(User user) throws JWTCreationException;
	public String getJWT(LoginCredentials credentials) throws JWTCreationException, AccountStatusException, UserRetrievalException;
	
}