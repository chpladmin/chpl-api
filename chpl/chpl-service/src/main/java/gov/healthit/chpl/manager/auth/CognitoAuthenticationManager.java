package gov.healthit.chpl.manager.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.CognitoNewPasswordRequiredRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.user.cognito.CognitoAuthenticationResponse;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;

@Log4j2
@Component
public class CognitoAuthenticationManager {

    private CognitoApiWrapper cognitoApiWrapper;
    private JWTUserConverterFacade jwtUserConverterFacade;

    @Autowired
    public CognitoAuthenticationManager(CognitoApiWrapper cognitoApiWrapper, JWTUserConverterFacade jwtUserConverterFacade) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.jwtUserConverterFacade = jwtUserConverterFacade;
    }

    public CognitoAuthenticationResponse authenticate(LoginCredentials credentials) throws PasswordResetRequiredException {
        AuthenticationResultType authResult = cognitoApiWrapper.authenticate(credentials);
        if (authResult == null) {
            return null;
        }

        JWTAuthenticatedUser jwtUser = jwtUserConverterFacade.getAuthenticatedUser(authResult.idToken());
        User user;
        try {
            user = cognitoApiWrapper.getUserInfo(jwtUser.getCognitoId());
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not decode JWT Token");
            return null;
        }

        return CognitoAuthenticationResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .user(user)
                .build();
    }

    public CognitoAuthenticationResponse newPassworRequiredChallenge(CognitoNewPasswordRequiredRequest request) throws PasswordResetRequiredException {
        AuthenticationResultType authResult = cognitoApiWrapper.respondToNewPasswordRequiredChallenge(request);
        if (authResult == null) {
            return null;
        }

        JWTAuthenticatedUser jwtUser = jwtUserConverterFacade.getAuthenticatedUser(authResult.idToken());
        User user;
        try {
            user = cognitoApiWrapper.getUserInfo(jwtUser.getCognitoId());
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not decode JWT Token");
            return null;
        }

        return CognitoAuthenticationResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .user(user)
                .build();
    }

}
