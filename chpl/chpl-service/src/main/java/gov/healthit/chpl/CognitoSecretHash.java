package gov.healthit.chpl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public final class CognitoSecretHash {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    private CognitoSecretHash() {}

    public static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String identifier) {
        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(identifier.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }

    public static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, UUID cognitoId) {
        return calculateSecretHash(userPoolClientId, userPoolClientSecret, cognitoId.toString());
    }
}