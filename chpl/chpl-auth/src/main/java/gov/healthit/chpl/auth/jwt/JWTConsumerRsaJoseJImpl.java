package gov.healthit.chpl.auth.jwt;

import gov.healthit.chpl.auth.AuthPropertiesConsumer;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
	
	Logger logger = LogManager.getLogger(JWTConsumerRsaJoseJImpl.class.getName());
	
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
		
		try
	    {
	        //Validate the JWT and process it
	        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
	        return jwtClaims.getClaimsMap();
	    }
	    catch (InvalidJwtException e)
	    {
	    	//log every time an expired / invalid
	    	// token is used.
	    	logger.error("Invalid JWT", e);
	        return null;
	    }
	}
}
