package gov.healthit.chpl.auth.jwt;


public interface JWKManager {
	
	public void createKey(String keyId, String algorithm);
	public JSONWebKey getKey();
	
}
