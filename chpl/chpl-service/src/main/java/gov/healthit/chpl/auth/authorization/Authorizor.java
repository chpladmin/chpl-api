package gov.healthit.chpl.auth.authorization;


import gov.healthit.chpl.auth.User;

public interface Authorizor {
	
	//Check JWT and build a user given a JWT
	public User getUser(String jwt);
	
}
