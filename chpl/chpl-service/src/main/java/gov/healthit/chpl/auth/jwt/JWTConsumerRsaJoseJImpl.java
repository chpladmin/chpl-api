package gov.healthit.chpl.auth.jwt;

import gov.healthit.chpl.auth.AuthPropertiesConsumer;

import java.util.Map;
import java.util.Properties;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class JWTConsumerRsaJoseJImpl extends AuthPropertiesConsumer implements JWTConsumer {

	
	@Autowired
	@Qualifier("RsaJose4JWebKey")
	JSONWebKey jwk;
	
	public Map<String, Object> consume(String jwt) {
		
		Properties jwtProps = this.getProps();
		
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
	    	.setRequireExpirationTime() // the JWT must have an expiration time
	    	.setAllowedClockSkewInSeconds(Integer.parseInt(jwtProps.getProperty("jwtAllowedClockSkew"))) // allow some leeway in validating time based claims to account for clock skew
	    	.setRequireSubject() // the JWT must have a subject claim
	    	.setExpectedIssuer(jwtProps.getProperty("jwtIssuer")) // whom the JWT needs to have been issued by
	    	.setExpectedAudience(jwtProps.getProperty("jwtAudience")) // to whom the JWT is intended for
	    	.setVerificationKey(jwk.getKey()) // verify the signature with the public key
	    	.build(); // create the JwtConsumer instance
		
		/*
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
	    	.setRequireExpirationTime() // the JWT must have an expiration time
	    	.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
	    	.setRequireSubject() // the JWT must have a subject claim
	    	.setExpectedIssuer("ONCCHPL") // whom the JWT needs to have been issued by
	    	.setExpectedAudience("ONCCHPL") // to whom the JWT is intended for
	    	.setVerificationKey(jwk.getKey()) // verify the signature with the public key
	    	.build(); // create the JwtConsumer instance
		*/
		try
	    {
	        //Validate the JWT and process it
	        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
	        System.out.println("JWT validation succeeded! " + jwtClaims);
	        return jwtClaims.getClaimsMap();
	    }
	    catch (InvalidJwtException e)
	    {
	    	//TODO: Add logging here
	        System.out.println("Invalid JWT! " + e);
	        return null;
	    }
	}
}
