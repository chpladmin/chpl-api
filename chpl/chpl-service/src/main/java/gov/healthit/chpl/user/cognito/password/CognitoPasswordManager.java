package gov.healthit.chpl.user.cognito.password;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoPasswordManager {

    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoForgotPasswordDAO cognitoForgotPasswordDAO;
    private CognitoForgotPasswordEmailer cognitoForgotPasswordEmailer;
    private CognitoPasswordChangedEmailer cognitoPasswordChangedEmailer;
    private Long forgotTokenValidInHours;

    @Autowired
    public CognitoPasswordManager(CognitoApiWrapper cognitoApiWrapper, CognitoForgotPasswordDAO cognitoForgotPasswordDAO,
            CognitoForgotPasswordEmailer cognitoForgotPasswordEmailer, CognitoPasswordChangedEmailer cognitoPasswordChangedEmailer,
            @Value("${cognito.forgotPassword.tokenValid}") Long forgotTokenValidInHours) {

        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoForgotPasswordEmailer = cognitoForgotPasswordEmailer;
        this.cognitoPasswordChangedEmailer = cognitoPasswordChangedEmailer;
        this.cognitoForgotPasswordDAO = cognitoForgotPasswordDAO;
        this.forgotTokenValidInHours = forgotTokenValidInHours;
    }

    @Transactional
    public void sendForgotPasswordEmail(String email) {
        try {
            if (cognitoApiWrapper.getUserInfo(email) != null) {
                CognitoForgotPassword forgotPassword = generateForgotPassword(email);
                cognitoForgotPasswordEmailer.sendEmail(forgotPassword);
            }
        } catch (EmailNotSentException | UserRetrievalException e) {
            LOGGER.error("Could not send 'forgot password' email to: {}", email);
        }
     }

    @Transactional
    public void setForgottenPassword(UUID forgotPasswordToken, String password) throws ValidationException, EmailNotSentException {
        CognitoForgotPassword forgotPassword = cognitoForgotPasswordDAO.getByToken(forgotPasswordToken);
        if (forgotPassword == null) {
            throw new ValidationException("Forgot Password Token is not valid.");
        }

        if (forgotPassword.isOlderThan(forgotTokenValidInHours)) {
            throw new ValidationException("Forgot Password Token has expired.");
        }

        cognitoApiWrapper.setUserPassword(forgotPassword.getEmail(), password);
        cognitoPasswordChangedEmailer.sendEmail(forgotPassword.getEmail());
    }

    private CognitoForgotPassword generateForgotPassword(String email) {
        return cognitoForgotPasswordDAO.create(CognitoForgotPassword.builder()
                .email(email)
                .token(UUID.randomUUID())
                .build());
    }
}
