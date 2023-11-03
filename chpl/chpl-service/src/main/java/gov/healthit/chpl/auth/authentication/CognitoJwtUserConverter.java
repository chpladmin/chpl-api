package gov.healthit.chpl.auth.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import gov.healthit.chpl.auth.jwt.CognitoRsaKeyProvider;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
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
    private String tokenizezRsaKeyUrl;

    public CognitoJwtUserConverter(@Value("${cognito.region}") String region, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.clientId}") String clientId, @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizezRsaKeyUrl) {
        this.region = region;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.tokenizezRsaKeyUrl = tokenizezRsaKeyUrl;
    }

    public User getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        JWTAuthenticatedUser user = new JWTAuthenticatedUser();
        DecodedJWT decodeJwt = decodeJwt(jwt);

        user.setAuthenticated(true);
        user.setId(decodeJwt.getClaim("id").asLong());
        user.setFullName(decodeJwt.getClaim("name").asString());
        user.setSubjectName(decodeJwt.getClaim("email").asString());

        decodeJwt.getClaim("cognito:groups").asList(String.class).stream()
                .forEach(role -> user.addPermission(new GrantedPermission(role)));

        return user;
    }

    private DecodedJWT decodeJwt(String jwt) {
        RSAKeyProvider keyProvider = new CognitoRsaKeyProvider(region, userPoolId, tokenizezRsaKeyUrl);
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        JWTVerifier jwtVerifier = JWT.require(algorithm)
            .withAudience(clientId)
            .build();

        DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

        return decodedJwt;
    }
}
