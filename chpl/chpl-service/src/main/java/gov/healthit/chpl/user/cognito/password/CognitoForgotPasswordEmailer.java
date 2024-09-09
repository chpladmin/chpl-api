package gov.healthit.chpl.user.cognito.password;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoForgotPasswordEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private ChplEmailFactory chplEmailFactory;
    private String chplUrl;
    private String subject;
    private String heading;
    private String paragraph1;
    private String paragraph2;
    private String paragraph3;
    private String paragraph4;


    @Autowired
    public CognitoForgotPasswordEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory,
            @Value("${chplUrlBegin}") String chplUrl,
            @Value("${cognito.forgotPassword.subject}") String subject,
            @Value("${cognito.forgotPassword.heading}") String heading,
            @Value("${cognito.forgotPassword.paragraph1}") String paragraph1,
            @Value("${cognito.forgotPassword.paragraph2}") String paragraph2,
            @Value("${cognito.forgotPassword.paragraph3}") String paragraph3,
            @Value("${cognito.forgotPassword.paragraph4}") String paragraph4) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.chplUrl = chplUrl;
        this.subject = subject;
        this.heading = heading;
        this.paragraph1 = paragraph1;
        this.paragraph2 = paragraph2;
        this.paragraph3 = paragraph3;
        this.paragraph4 = paragraph4;
    }

    public void sendEmail(CognitoForgotPassword forgotPassword) throws EmailNotSentException {
        String link = String.format("%s/#/forgot-password/%s", chplUrl, forgotPassword.getToken().toString());

        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(heading)
                .paragraph("", paragraph1)
                .paragraph("", String.format(paragraph2, link))
                .paragraph("", String.format(paragraph3, link))
                .paragraph("", paragraph4)
                .footer(PublicFooter.class)
                .build();

        chplEmailFactory.emailBuilder()
            .recipients(List.of(forgotPassword.getEmail()))
            .subject(subject)
            .htmlMessage(htmlMessage)
            .sendEmail();
    }

}
