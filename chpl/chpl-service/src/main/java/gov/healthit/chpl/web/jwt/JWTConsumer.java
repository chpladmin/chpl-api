package gov.healthit.chpl.web.jwt;

public interface JWTConsumer {

	public boolean verify(String jwt);
	
}
