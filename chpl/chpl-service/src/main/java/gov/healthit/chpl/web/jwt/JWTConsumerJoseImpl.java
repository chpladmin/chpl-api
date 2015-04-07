package gov.healthit.chpl.web.jwt;

import java.security.Key;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;




public class JWTConsumerJoseImpl implements JWTConsumer {

	
	JwtConsumer jwtConsumer = null;
	
	JWTConsumerJoseImpl(JsonWebKey jwk) throws JoseException{
		
		jwtConsumer = new JwtConsumerBuilder()
        	.setRequireExpirationTime() // the JWT must have an expiration time
        	.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
        	.setRequireSubject() // the JWT must have a subject claim
        	.setExpectedIssuer("ONCCHPL") // whom the JWT needs to have been issued by
        	.setExpectedAudience("ONCCHPL") // to whom the JWT is intended for
        	.setVerificationKey(jwk.getKey()) // verify the signature with the public key
        	.build(); // create the JwtConsumer instance
	}
	
	@Override
	public boolean verify(String jwt){
		
		try
	    {
	        //Validate the JWT and process it to the Claims
	        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
	        System.out.println("JWT validation succeeded! " + jwtClaims);
	        return true;
	    }
	    catch (InvalidJwtException e)
	    {
	        System.out.println("Invalid JWT! " + e);
	        return false;
	    }
		
	}

	
	
	
}
