package gov.healthit.chpl.auth.jwt;


import java.util.Map;

public interface JWTConsumer {

	public Map<String, Object> consume(String jwt);
	
}
