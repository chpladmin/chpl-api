package gov.healthit.chpl.auth.jwt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

@Service("RsaJose4JWebKey")
public class JSONWebKeyRsaJoseJImpl implements JSONWebKey {

	private String keyLocation = "D:\\CHPL\\Keys\\JSONRsaJoseJWebKey.txt";
	RsaJsonWebKey rsaJsonWebKey = null;
	
	
	public JSONWebKeyRsaJoseJImpl() {
		
		if (rsaJsonWebKey == null){
			
			try {
				loadSavedKey(keyLocation);
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				try {
			        RsaKeyUtil keyUtil = new RsaKeyUtil();
			        KeyPair keyPair;
					keyPair = keyUtil.generateKeyPair(2048);
			        rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
			        rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
			        saveKey(this.keyLocation);
				} catch (JoseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public void createOrLoadKey(){
		if (rsaJsonWebKey == null){
			
			try {
				loadSavedKey(keyLocation);
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				try {
			        RsaKeyUtil keyUtil = new RsaKeyUtil();
			        KeyPair keyPair;
					keyPair = keyUtil.generateKeyPair(2048);
			        rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
			        rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
			        saveKey(this.keyLocation);
				} catch (JoseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			FileOutputStream fileOut = new FileOutputStream(keyPairPath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(rsaJsonWebKey);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadSavedKey(String keyPairPath) throws IOException, ClassNotFoundException {
		
		FileInputStream fileIn = new FileInputStream(keyPairPath);
		ObjectInputStream is = new ObjectInputStream(fileIn);
		
		//KeyPair keyPair = (KeyPair) is.readObject();
        //try {
		//	rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
		///} catch (JoseException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		rsaJsonWebKey = (RsaJsonWebKey) is.readObject();
        //rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
		is.close();
		fileIn.close();
		
	}
}
