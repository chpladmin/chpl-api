package gov.healthit.chpl.auth.authorization;


import gov.healthit.chpl.auth.User;
import gov.healthit.chpl.auth.jwt.JWTValidationException;

public interface Authorizor {
	
	//Check JWT and build a user given a JWT
	public User getUser(String jwt) throws JWTValidationException;
	
}
