package gov.healthit.chpl.user.cognito;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoConfirmEmailEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    public CognitoConfirmEmailEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
    }

    public void sendConfirmationEmail(LoginCredentials credentials) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading("Confirm CHPL Account")
                .paragraph("Please go to the CHPL and login with this one-time password.",
                        String.format("Email: %s<br />Confirmation Code: %s", credentials.getUserName(), credentials.getPassword()))
                .footer(PublicFooter.class)
                .build();
        LOGGER.info("Created HTML Message for " + credentials.getUserName());

        chplEmailFactory.emailBuilder()
            .recipients(List.of(credentials.getUserName()))
            .subject("Confirm CHPL Account")
            .htmlMessage(htmlMessage)
            .sendEmail();
        LOGGER.info("Sent email to " + credentials.getUserName());
    }
}
