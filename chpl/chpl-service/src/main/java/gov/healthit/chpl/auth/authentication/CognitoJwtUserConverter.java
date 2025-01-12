package gov.healthit.chpl.auth.authentication;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import gov.healthit.chpl.auth.jwt.CognitoRsaKeyProvider;
import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CognitoJwtUserConverter implements JWTUserConverter {
    private String region;
    private String userPoolId;
    private String tokenizeRsaKeyUrl;

    public CognitoJwtUserConverter(@Value("${cognito.region}") String region,
            @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl) {
        this.region = region;
        this.userPoolId = userPoolId;
        this.tokenizeRsaKeyUrl = tokenizeRsaKeyUrl;
    }

    @Override
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        try {
            DecodedJWT decodedJwt = decodeJwt(jwt);
            if (decodedJwt.getClaims().size() != 0) {
                return JWTAuthenticatedUser.builder()
                        .authenticationSystem(AuthenticationSystem.COGNITO)
                        .authenticated(true)
                        .cognitoId(UUID.fromString(decodedJwt.getSubject()))
                        .build();
            } else {
                throw new JWTValidationException("Invalid authentication token.");
            }
        } catch (JWTValidationException e) {
            LOGGER.error("JWT Validation Exception", e);
            throw e;

        } catch (Exception e) {
            LOGGER.error("Error decoding JWT token", e);
            return null;
        }
    }

    private DecodedJWT decodeJwt(String jwt) {
        RSAKeyProvider keyProvider = new CognitoRsaKeyProvider(region, userPoolId, tokenizeRsaKeyUrl);
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        JWTVerifier jwtVerifier = JWT.require(algorithm)
            .acceptLeeway(30000) //allows for the CHPL server clock and AWS server clock to be off by 30 seconds
            .build();

        DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

        return decodedJwt;
    }

    @Override
    public JWTAuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException {
        throw new NotImplementedException("CognitoJwtUserConverter.getImpersonatingUser() has not been implemented.");
    }
}
