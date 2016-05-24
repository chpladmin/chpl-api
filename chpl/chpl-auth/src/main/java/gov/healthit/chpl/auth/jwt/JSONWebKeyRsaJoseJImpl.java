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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("RsaJose4JWebKey")
public class JSONWebKeyRsaJoseJImpl implements JSONWebKey {

	@Autowired Environment env;
	RsaJsonWebKey rsaJsonWebKey = null;
	
	Logger logger = LogManager.getLogger(JSONWebKeyRsaJoseJImpl.class.getName());
	
	public JSONWebKeyRsaJoseJImpl() {
	}
	
	@PostConstruct
	public void createOrLoadKey(){
		String keyLocation = env.getProperty("keyLocation");
		
		if (rsaJsonWebKey == null){
			
			try {
				loadSavedKey(keyLocation);
			} catch (ClassNotFoundException | IOException e1) {
				
				logger.error("Key not found or error loading key. Creating new key. " , e1);
				
				try {
			        RsaKeyUtil keyUtil = new RsaKeyUtil();
			        KeyPair keyPair;
					keyPair = keyUtil.generateKeyPair(2048);
			        rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
			        rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
			        saveKey(keyLocation);
				} catch (JoseException e) {
					
					logger.error("Error creating key: " , e1);
					
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

	
	public void saveKey(String keyPairPath){
		
		try {
			
			File file = new File(keyPairPath);
			file.getParentFile().mkdirs();
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(rsaJsonWebKey);
			out.close();
			fileOut.close();
			
		} catch (IOException e) {
			logger.error("Error saving key: " , e);
		}
	}
	
	public void loadSavedKey(String keyPairPath) throws IOException, ClassNotFoundException {
		
		FileInputStream fileIn = new FileInputStream(keyPairPath);
		ObjectInputStream is = new ObjectInputStream(fileIn);
		
		rsaJsonWebKey = (RsaJsonWebKey) is.readObject();
		
		is.close();
		fileIn.close();
		
	}
}
