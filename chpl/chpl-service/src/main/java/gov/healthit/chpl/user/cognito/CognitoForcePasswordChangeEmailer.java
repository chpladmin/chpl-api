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
public class CognitoForcePasswordChangeEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private ChplEmailFactory chplEmailFactory;
    private String chplUrl;
    private String subject;
    private String heading;
    private String paragraph1;
    private String paragraph2;
    private String paragraph3;

    @Autowired
    public CognitoForcePasswordChangeEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory,
            @Value("${chplUrlBegin}") String chplUrl,
            @Value("${cognito.forcePasswordChange.subject}") String subject,
            @Value("${cognito.forcePasswordChange.heading}") String heading,
            @Value("${cognito.forcePasswordChange.paragraph1}") String paragraph1,
            @Value("${cognito.forcePasswordChange.paragraph2}") String paragraph2,
            @Value("${cognito.forcePasswordChange.paragraph3}") String paragraph3) {

        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.chplUrl = chplUrl;
        this.subject = subject;
        this.heading = heading;
        this.paragraph1 = paragraph1;
        this.paragraph2 = paragraph2;
        this.paragraph3 = paragraph3;
    }

    public void sendEmail(LoginCredentials credentials) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(heading)
                .paragraph("", String.format(paragraph1, chplUrl))
                .paragraph("", String.format(paragraph2, credentials.getUserName(), credentials.getPassword()))
                .paragraph("", paragraph3)
                .footer(PublicFooter.class)
                .build();

        chplEmailFactory.emailBuilder()
            .recipients(List.of(credentials.getUserName()))
            .subject(subject)
            .htmlMessage(htmlMessage)
            .sendEmail();
    }

}
