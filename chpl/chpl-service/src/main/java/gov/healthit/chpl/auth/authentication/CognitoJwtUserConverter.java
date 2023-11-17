package gov.healthit.chpl.auth.authentication;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import gov.healthit.chpl.auth.jwt.CognitoRsaKeyProvider;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.CognitoJWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoJwtUserConverter {
    private String region;
    private String userPoolId;
    private String clientId;
    private String tokenizeRsaKeyUrl;

    public CognitoJwtUserConverter(@Value("${cognito.region}") String region, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.clientId}") String clientId, @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl) {
        this.region = region;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.tokenizeRsaKeyUrl = tokenizeRsaKeyUrl;
    }

    public User getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        CognitoJWTAuthenticatedUser user = new CognitoJWTAuthenticatedUser();
        DecodedJWT decodeJwt = decodeJwt(jwt);

        user.setAuthenticated(true);
        user.setSsoId(UUID.fromString(decodeJwt.getSubject()));
        user.setFullName(decodeJwt.getClaim("name").asString());
        user.setSubjectName(decodeJwt.getClaim("email").asString());

        decodeJwt.getClaim("cognito:groups").asList(String.class).stream()
                .forEach(role -> user.addPermission(new GrantedPermission(role)));

        return user;
    }

    private DecodedJWT decodeJwt(String jwt) {
        RSAKeyProvider keyProvider = new CognitoRsaKeyProvider(region, userPoolId, tokenizeRsaKeyUrl);
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        JWTVerifier jwtVerifier = JWT.require(algorithm)
            .withAudience(clientId)
            .build();

        DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

        return decodedJwt;
    }
}
