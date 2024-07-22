package gov.healthit.chpl.user.cognito;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private String chplUrl;

    @Autowired
    public CognitoConfirmEmailEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory, @Value("${chplUrlBegin}") String chplUrl) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.chplUrl = chplUrl;
    }

    public void sendConfirmationEmail(LoginCredentials credentials) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading("Confirm CHPL Account")
                .paragraph(String.format("Please go to the <a href='%s'>CHPL</a> and login with this one-time password.", chplUrl),
                        String.format("Email: %s<br />One-time Password: %s", credentials.getUserName(), credentials.getPassword()))
                .paragraph("", "Your one-time password is valid for 7 days.")
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
