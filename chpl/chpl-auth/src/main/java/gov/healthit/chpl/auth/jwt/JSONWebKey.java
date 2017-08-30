package gov.healthit.chpl.auth.jwt;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface JSONWebKey {

	public String getKeyId();
	public String getAlgorithm();
	public Key getKey();
	public PrivateKey getPrivateKey();
	public PublicKey getPublicKey();
	
	public void saveKey(String keyPairPath);
	public void loadSavedKey(String keyPairPath) throws IOException, ClassNotFoundException;
	public void createOrLoadKey();
}
