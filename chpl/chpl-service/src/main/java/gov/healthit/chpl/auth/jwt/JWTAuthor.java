package gov.healthit.chpl.auth.jwt;

import java.util.List;
import java.util.Map;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dto.auth.UserDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class JWTAuthor {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("RsaJose4JWebKey")
    private JSONWebKey jwk;

    public String createJWT(UserDTO user, Map<String, String> stringClaims, Map<String, List<String>> listClaims) {
        // Create the Claims, which will be the content of the JWT
        JwtClaims claimsObj = new JwtClaims();
        // who creates the token and signs it
        claimsObj.setIssuer(env.getProperty("jwtIssuer"));
        // to whom the token is intended to be sent
        claimsObj.setAudience(env.getProperty("jwtAudience"));
        // time when the token will expire (from now)
        claimsObj.setExpirationTimeMinutesInTheFuture(
                Integer.parseInt(env.getProperty("jwtExpirationTimeMinutesInTheFuture")));
        // a unique identifier for the token
        claimsObj.setGeneratedJwtId();
        // when the token was issued/created (now)
        claimsObj.setIssuedAtToNow();
        // time before which the token is not yet valid (minutes ago)
        claimsObj.setNotBeforeMinutesInThePast(Integer.parseInt(env.getProperty("jwtNotBeforeMinutesInThePast")));
        // the subject/principal is whom the token is about
        claimsObj.setSubject(user.getEmail());
        // any other claims made about the user that have single string values
        if (stringClaims != null) {
            for (Map.Entry<String, String> claim : stringClaims.entrySet()) {
                claimsObj.setStringClaim(claim.getKey(), claim.getValue());
            }
        }
        //any other claims made about the user that have multiple values
        if (listClaims != null) {
            for (Map.Entry<String, List<String>> claim : listClaims.entrySet()) {
                claimsObj.setStringListClaim(claim.getKey(), claim.getValue());
            }
        }

        // A JWT is a JWS and/or a JWE with JSON claims as the payload.
        // In this example it is a JWS so we create a JsonWebSignature object.
        JsonWebSignature jws = new JsonWebSignature();

        // The payload of the JWS is JSON content of the JWT Claims
        jws.setPayload(claimsObj.toJson());

        // The JWT is signed using the private key
        jws.setKey(jwk.getPrivateKey());

        // Set the Key ID (kid) header because it's just the polite thing to do.
        // We only have one key in this example but a using a Key ID helps
        // facilitate a smooth key rollover process
        // jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());

        // Set the signature algorithm on the JWT/JWS that will integrity
        // protect the claims
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        // Sign the JWS and produce the compact serialization or the complete
        // JWT/JWS
        // representation, which is a string consisting of three dot ('.')
        // separated
        // base64url-encoded parts in the form Header.Payload.Signature
        // If you wanted to encrypt it, you can simply set this jwt as the
        // payload
        // of a JsonWebEncryption object and set the cty (Content Type) header
        // to "jwt".
        String jwt = null;
        try {
            jwt = jws.getCompactSerialization();
        } catch (JoseException e) {
            LOGGER.error("Token creation error", e);
        }

        return jwt;
    }
}
