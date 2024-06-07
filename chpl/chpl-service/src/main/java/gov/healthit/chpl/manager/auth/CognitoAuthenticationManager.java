package gov.healthit.chpl.manager.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.user.cognito.CognitoAuthenticationResponse;
import gov.healthit.chpl.user.cognito.CognitoUserManager;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordResetRequiredException;

@Log4j2
@Component
public class CognitoAuthenticationManager {

    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoUserManager cognitoUserManager;

    @Autowired
    public CognitoAuthenticationManager(CognitoApiWrapper cognitoApiWrapper, CognitoUserManager cognitoUserManager) {
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoUserManager = cognitoUserManager;
    }

    public CognitoAuthenticationResponse authenticate(LoginCredentials credentials) throws PasswordResetRequiredException {
        AuthenticationResultType authResult = cognitoApiWrapper.authenticate(credentials);
        if (authResult == null) {
            return null;
        }

        User user = null; //cognitoUserManager.getUserInfo(null)

        return CognitoAuthenticationResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .user(user)
                .build();
    }
}
