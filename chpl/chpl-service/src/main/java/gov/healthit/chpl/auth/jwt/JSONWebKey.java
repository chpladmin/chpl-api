package gov.healthit.chpl.auth.jwt;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface JSONWebKey {

    String getKeyId();

    String getAlgorithm();

    Key getKey();

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    void saveKey(String keyPairPath);

    void loadSavedKey(String keyPairPath) throws IOException, ClassNotFoundException;

    void createOrLoadKey();
}
