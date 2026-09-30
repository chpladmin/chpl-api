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
    private String confirmEmailSubject;
    private String confirmationEmailBodyParagraph1;
    private String confirmationEmailBodyParagraph2;
    private String chplUrl;

    @Autowired
    public CognitoConfirmEmailEmailer(ChplHtmlEmailBuilder htmlEmailBuilder,
            ChplEmailFactory chplEmailFactory,
            @Value("${account.conirmation.title}") String confirmEmailSubject,
            @Value("${account.confirmation.paragraph1}") String confirmationEmailBodyParagraph1,
            @Value("${account.confirmation.paragraph2}") String confirmationEmailBodyParagraph2,
            @Value("${chplUrlBegin}") String chplUrl) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.confirmEmailSubject = confirmEmailSubject;
        this.confirmationEmailBodyParagraph1 = confirmationEmailBodyParagraph1;
        this.confirmationEmailBodyParagraph2 = confirmationEmailBodyParagraph2;
        this.chplUrl = chplUrl;
    }

    public void sendConfirmationEmail(LoginCredentials credentials) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(confirmEmailSubject)
                .paragraph(null, String.format(confirmationEmailBodyParagraph1, chplUrl, credentials.getUserName(), credentials.getPassword()))
                .paragraph(null, confirmationEmailBodyParagraph2)
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
