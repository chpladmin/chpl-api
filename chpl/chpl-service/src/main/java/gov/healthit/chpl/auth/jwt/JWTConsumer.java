package gov.healthit.chpl.auth.jwt;

import java.util.Map;

import org.jose4j.jwt.consumer.InvalidJwtException;

public interface JWTConsumer {

    /**
     * Returns map of JWT claims, or returns null if token is invalid
     */
    Map<String, Object> consume(String jwt) throws InvalidJwtException;
}
