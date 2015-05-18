package gov.healthit.chpl.auth.authorization;


import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.user.User;


public interface JWTUserConverter {
	
	//Check JWT and build a user given a JWT
	public User getUser(String jwt) throws JWTValidationException;
	
}
