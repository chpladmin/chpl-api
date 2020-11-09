package gov.healthit.chpl.auth.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.PostConstruct;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("RsaJose4JWebKey")
public class JSONWebKeyRsaJoseJImpl implements JSONWebKey {

    @Autowired
    private Environment env = null;
    private RsaJsonWebKey rsaJsonWebKey = null;
    private static final int KEY_BIT_LENGTH = 2048;

    public JSONWebKeyRsaJoseJImpl() {
    }

    @PostConstruct
    /**
     * If there is no current key, this method creates one and then places it into the
     * keyLocation.
     */
    public void createOrLoadKey() {
        String keyLocation = env.getProperty("keyLocation");

        if (rsaJsonWebKey == null) {

            try {
                loadSavedKey(keyLocation);
            } catch (ClassNotFoundException | IOException e1) {

                LOGGER.error("Key not found or error loading key. Creating new key. ", e1);

                try {
                    RsaKeyUtil keyUtil = new RsaKeyUtil();
                    KeyPair keyPair;
                    keyPair = keyUtil.generateKeyPair(KEY_BIT_LENGTH);
                    rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
                    rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
                    saveKey(keyLocation);
                } catch (JoseException e) {

                    LOGGER.error("Error creating key: ", e);

                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String getKeyId() {
        return rsaJsonWebKey.getKeyId();
    }

    @Override
    public String getAlgorithm() {
        return rsaJsonWebKey.getAlgorithm();
    }

    @Override
    public Key getKey() {
        return rsaJsonWebKey.getKey();
    }

    @Override
    public PrivateKey getPrivateKey() {
        return rsaJsonWebKey.getPrivateKey();
    }

    @Override
    public PublicKey getPublicKey() {
        return rsaJsonWebKey.getPublicKey();
    }

    /**
     * Writes a key to the location specified in keyLocation
     * @param keyPairPath location to write the key to
     */
    public void saveKey(final String keyPairPath) {

        try {

            File file = new File(keyPairPath);
            file.getParentFile().mkdirs();
            try (FileOutputStream fileOut = new FileOutputStream(file);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(rsaJsonWebKey);
            }

        } catch (IOException e) {
            LOGGER.error("Error saving key: ", e);
        }
    }

    /**
     * Reads the key from the location specified in keyLocation
     * @param keyPairPath location to read the key from
     */
    public void loadSavedKey(String keyPairPath) throws IOException, ClassNotFoundException {

        try (FileInputStream fileIn = new FileInputStream(keyPairPath);
                ObjectInputStream is = new ObjectInputStream(fileIn)) {
            rsaJsonWebKey = (RsaJsonWebKey) is.readObject();
        }

    }
}
