package gov.healthit.chpl.auth.authentication;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
    private String clientId;
    private String tokenizeRsaKeyUrl;

    public CognitoJwtUserConverter(@Value("${cognito.region}") String region, @Value("${cognito.userPoolId}") String userPoolId,
            @Value("${cognito.clientId}") String clientId, @Value("${cognito.tokenizezRsaKeyUrl}") String tokenizeRsaKeyUrl) {
        this.region = region;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.tokenizeRsaKeyUrl = tokenizeRsaKeyUrl;
    }

    @Override
    public JWTAuthenticatedUser getAuthenticatedUser(String jwt) throws JWTValidationException, MultipleUserAccountsException {
        try {
            DecodedJWT decodeJwt = decodeJwt(jwt);
            if (decodeJwt.getClaims().size() != 0) {
                return JWTAuthenticatedUser.builder()
                        .authenticationSystem(AuthenticationSystem.COGNITO)
                        .authenticated(true)
                        .cognitoId(UUID.fromString(decodeJwt.getSubject()))
                        .subjectName(decodeJwt.getClaim("email").asString())
                        .fullName(decodeJwt.getClaim("name").asString())
                        .email(decodeJwt.getClaim("email").asString())
                        .organizationIds(
                                decodeJwt.getClaims().containsKey("custom:organizations")
                                        ? Stream.of(decodeJwt.getClaim("custom:organizations").asString().split(","))
                                                .map(Long::valueOf)
                                                .toList()
                                        : null)
                        .authorities(decodeJwt.getClaim("cognito:groups").asList(String.class).stream()
                                .filter(group -> !group.endsWith("-env")) //Remove environment related groups
                                .map(group -> new SimpleGrantedAuthority(group))
                                .collect(Collectors.toSet()))
                        .build();
            } else {
                throw new JWTValidationException("Invalid authentication token.");
            }
        } catch (JWTValidationException e) {
            throw e;

        } catch (Exception e) {
            return null;
        }
    }

    private DecodedJWT decodeJwt(String jwt) {
        RSAKeyProvider keyProvider = new CognitoRsaKeyProvider(region, userPoolId, tokenizeRsaKeyUrl);
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        JWTVerifier jwtVerifier = JWT.require(algorithm)
            //.withAudience(clientId)
            .build();

        DecodedJWT decodedJwt = jwtVerifier.verify(jwt);

        return decodedJwt;
    }

    @Override
    public JWTAuthenticatedUser getImpersonatingUser(String jwt) throws JWTValidationException {
        throw new NotImplementedException("CognitoJwtUserConverter.getImpersonatingUser() has not been implemented.");
    }
}
