package gov.healthit.chpl.user.cognito.authentication;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.auth.CognitoNewPasswordRequiredRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

@Log4j2
@Component
public class CognitoAuthenticationManager {
    public static final int MIN_PASSWORD_STRENGTH = 3;

    private CognitoApiWrapper cognitoApiWrapper;
    private JWTUserConverterFacade jwtUserConverterFacade;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CognitoAuthenticationManager(CognitoApiWrapper cognitoApiWrapper, JWTUserConverterFacade jwtUserConverterFacade, ErrorMessageUtil errorMessageUtil) {

        this.cognitoApiWrapper = cognitoApiWrapper;
        this.jwtUserConverterFacade = jwtUserConverterFacade;
        this.errorMessageUtil = errorMessageUtil;
    }

    public CognitoAuthenticationResponse authenticate(LoginCredentials credentials) throws CognitoAuthenticationChallengeException{

        try {
            AuthenticationResultType authResult = cognitoApiWrapper.authenticate(credentials);
            if (authResult == null) {
                return null;
            }

            JWTAuthenticatedUser jwtUser = jwtUserConverterFacade.getAuthenticatedUser(authResult.accessToken());
            User user = cognitoApiWrapper.getUserInfo(jwtUser.getCognitoId());
            return CognitoAuthenticationResponse.builder()
                    .accessToken(authResult.accessToken())
                    .idToken(authResult.idToken())
                    .refreshToken(authResult.refreshToken())
                    .user(user)
                    .build();
        } catch (UserRetrievalException | JWTValidationException | MultipleUserAccountsException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public void invalidateTokensForUser(String email) {
        cognitoApiWrapper.invalidateTokensForUser(email);
    }

    public CognitoAuthenticationResponse newPassworRequiredChallenge(CognitoNewPasswordRequiredRequest request) throws ValidationException {
        if (!validatePaswordStrength(request)) {
            throw new ValidationException(errorMessageUtil.getMessage("auth.passwordComplexity"));
        }

        AuthenticationResultType authResult = cognitoApiWrapper.respondToNewPasswordRequiredChallenge(request);
        if (authResult == null) {
            return null;
        }

        return CognitoAuthenticationResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .user(getUserBasedOnIdToken(authResult.idToken()))
                .build();
    }

    public CognitoAuthenticationResponse refreshAuthenticationTokens(String refreshToken, UUID cognitoId) {
        AuthenticationResultType authResult = cognitoApiWrapper.refreshToken(refreshToken, cognitoId);
        return CognitoAuthenticationResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .build();
    }


    private Boolean validatePaswordStrength(CognitoNewPasswordRequiredRequest request) {
        ArrayList<String> badWords = new ArrayList<String>();
        badWords.add("chpl");
        badWords.add(request.getUserName());

        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(request.getPassword(), badWords);

        if (strength.getScore() < MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            return false;
        }
        return true;
    }

    private User getUserBasedOnIdToken(String accessToken) {
        try {
            JWTAuthenticatedUser jwtUser = jwtUserConverterFacade.getAuthenticatedUser(accessToken);
            return cognitoApiWrapper.getUserInfo(jwtUser.getCognitoId());
        } catch (UserRetrievalException | JWTValidationException | MultipleUserAccountsException e) {
            LOGGER.error("Could not decode JWT Token");
            return null;
        }
    }
}
