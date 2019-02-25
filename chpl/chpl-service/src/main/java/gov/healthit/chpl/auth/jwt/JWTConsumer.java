package gov.healthit.chpl.auth.jwt;


import java.util.Map;

public interface JWTConsumer {

	/**
	 * Returns map of JWT claims, or returns null if token is invalid 
	 */
	public Map<String, Object> consume(String jwt);
	
}
