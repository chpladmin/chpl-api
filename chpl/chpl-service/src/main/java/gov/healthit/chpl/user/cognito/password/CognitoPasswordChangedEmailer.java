package gov.healthit.chpl.user.cognito.password;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;

@Component
public class CognitoPasswordChangedEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private ChplEmailFactory chplEmailFactory;
    private String subject;
    private String heading;
    private String paragraph1;
    private String paragraph2;
    private String chplUrlBegin;
    private String publicFeedbackUrl;

    @Autowired
    public CognitoPasswordChangedEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory,
            @Value("${cognito.changePassword.subject}") String subject,
            @Value("${cognito.changePassword.heading}") String heading,
            @Value("${cognito.changePassword.paragraph1}") String paragraph1,
            @Value("${cognito.changePassword.paragraph2}") String paragraph2,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${contact.publicUrl}") String publicFeedbackUrl) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.subject = subject;
        this.heading = heading;
        this.paragraph1 = paragraph1;
        this.paragraph2 = paragraph2;
        this.chplUrlBegin = chplUrlBegin;
        this.publicFeedbackUrl = publicFeedbackUrl;
    }

    public void sendEmail(String email) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(heading)
                .paragraph("", paragraph1)
                .paragraph("", String.format(paragraph2, chplUrlBegin, publicFeedbackUrl))
                .footer(PublicFooter.class)
                .build();

        chplEmailFactory.emailBuilder()
            .recipients(List.of(email))
            .subject(subject)
            .htmlMessage(htmlMessage)
            .sendEmail();
    }

}
