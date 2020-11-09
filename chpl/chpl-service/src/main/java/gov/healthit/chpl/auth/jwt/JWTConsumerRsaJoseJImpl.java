package gov.healthit.chpl.auth.jwt;

import java.util.Map;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class JWTConsumerRsaJoseJImpl implements JWTConsumer {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("RsaJose4JWebKey")
    JSONWebKey jwk;


    public Map<String, Object> consume(String jwt) throws InvalidJwtException {

        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime() // the JWT must have an expiration time
                .setAllowedClockSkewInSeconds(Integer.parseInt(env.getProperty("jwtAllowedClockSkew"))) // allow some leeway
                                                                                                        // in validating time
                                                                                                        // based claims to
                                                                                                        // account for clock
                                                                                                        // skew
                .setRequireSubject() // the JWT must have a subject claim
                .setExpectedIssuer(env.getProperty("jwtIssuer")) // whom the JWT needs to have been issued by
                .setExpectedAudience(env.getProperty("jwtAudience")) // to whom the JWT is intended for
                .setVerificationKey(jwk.getKey()) // verify the signature with the public key
                .build(); // create the JwtConsumer instance

        try {
            // Validate the JWT and process it
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            return jwtClaims.getClaimsMap();
        } catch (InvalidJwtException e) {
            LOGGER.error("Invalid JWT - " + e.getMessage());
            throw new InvalidJwtException("Invalid JWT - failed in JWTConsumerRsaJoseJImpl.");
        }
    }
}
