package gov.healthit.chpl.auth.jwt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

@Service
public class JWTAuthorImpl implements JWTAuthor {
	
	RsaJsonWebKey rsaJsonWebKey = null;
	String keyLocation = "D:\\CHPL\\Keys\\key.txt";
	
	JWTAuthorImpl(){};
	
	public void createKey() {

		try {
	        RsaKeyUtil keyUtil = new RsaKeyUtil();
	        KeyPair keyPair;
			keyPair = keyUtil.generateKeyPair(2048);
	        rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
	        rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
	        writeKey(this.keyLocation);
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void writeKey(String keyPairPath) {
		
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
	
	public void readKey(String keyPairPath) throws IOException, ClassNotFoundException {
		
		FileInputStream fileIn = new FileInputStream(keyPairPath);
		ObjectInputStream is = new ObjectInputStream(fileIn);
		
		KeyPair keyPair = (KeyPair) is.readObject();
        try {
			rsaJsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(keyPair.getPublic());
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        rsaJsonWebKey.setPrivateKey(keyPair.getPrivate());
		is.close();
		fileIn.close();
		
	}
	
	
	public String createJWT(String subject, Map<String, List<String> > claims) {
		
		if (rsaJsonWebKey == null){
			createKey();
		}
		
	    // Create the Claims, which will be the content of the JWT
	    JwtClaims claimsObj = new JwtClaims();
	    claimsObj.setIssuer("ONCCHPL");  // who creates the token and signs it
	    claimsObj.setAudience("ONCCHPL"); // to whom the token is intended to be sent
	    claimsObj.setExpirationTimeMinutesInTheFuture(30); // time when the token will expire (10 minutes from now)
	    claimsObj.setGeneratedJwtId(); // a unique identifier for the token
	    claimsObj.setIssuedAtToNow();  // when the token was issued/created (now)
	    claimsObj.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
	    claimsObj.setSubject(subject); // the subject/principal is whom the token is about

	    for (Map.Entry<String, List<String> > claim : claims.entrySet())
	    {
	    	claimsObj.setStringListClaim(claim.getKey(), claim.getValue());	
	    }
	    
	    // A JWT is a JWS and/or a JWE with JSON claims as the payload.
	    // In this example it is a JWS so we create a JsonWebSignature object.
	    JsonWebSignature jws = new JsonWebSignature();

	    // The payload of the JWS is JSON content of the JWT Claims
	    jws.setPayload(claimsObj.toJson());

	    // The JWT is signed using the private key
	    jws.setKey(rsaJsonWebKey.getPrivateKey());

	    // Set the Key ID (kid) header because it's just the polite thing to do.
	    // We only have one key in this example but a using a Key ID helps
	    // facilitate a smooth key rollover process
	    //jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());

	    // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
	    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

	    // Sign the JWS and produce the compact serialization or the complete JWT/JWS
	    // representation, which is a string consisting of three dot ('.') separated
	    // base64url-encoded parts in the form Header.Payload.Signature
	    // If you wanted to encrypt it, you can simply set this jwt as the payload
	    // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
	    String jwt = null;
		try {
			jwt = jws.getCompactSerialization();
		} catch (JoseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return jwt;

	}
	
}
