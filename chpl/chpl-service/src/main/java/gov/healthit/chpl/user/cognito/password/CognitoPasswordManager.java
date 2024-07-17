package gov.healthit.chpl.user.cognito.password;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoPasswordManager {

    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoForgotPasswordDAO cognitoForgotPasswordDAO;
    private CognitoForgotPasswordEmailer cognitoForgotPasswordEmailer;

    @Autowired
    public CognitoPasswordManager(CognitoApiWrapper cognitoApiWrapper, CognitoForgotPasswordDAO cognitoForgotPasswordDAO,
            CognitoForgotPasswordEmailer cognitoForgotPasswordEmailer) {

        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoForgotPasswordEmailer = cognitoForgotPasswordEmailer;
        this.cognitoForgotPasswordDAO = cognitoForgotPasswordDAO;
    }

    @Transactional
    public void sendForgotPasswordEmail(String email) {
        CognitoForgotPassword forgotPassword = generateForgotPassword(email);
        try {
            cognitoForgotPasswordEmailer.sendEmail(forgotPassword);
        } catch (EmailNotSentException e) {
            LOGGER.error("Could not send 'forgot password' email to: {}", email);
        }
     }

    @Transactional
    public void setForgottenPassword(UUID forgotPasswordToken, String password) throws ValidationException {
        CognitoForgotPassword forgotPassword = cognitoForgotPasswordDAO.getByToken(forgotPasswordToken);
        if (forgotPassword == null) {
            throw new ValidationException("Token is not valid for forgot password.");
        }

        if (forgotPassword.isOlderThan(1L)) {
            throw new ValidationException("Token is has expired.");
        }

        cognitoApiWrapper.setUserPassword(forgotPassword.getEmail(), password);
    }

    private CognitoForgotPassword generateForgotPassword(String email) {
        return cognitoForgotPasswordDAO.create(CognitoForgotPassword.builder()
                .email(email)
                .token(UUID.randomUUID())
                .build());
    }
}
