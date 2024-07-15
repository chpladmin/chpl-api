package gov.healthit.chpl.user.cognito;

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

    @Autowired
    public CognitoForgotPasswordEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory, @Value("${chplUrlBegin}") String chplUrl) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;
        this.chplUrl = chplUrl;
    }

    public void sendEmail(CognitoForgotPassword forgotPassword) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading("Forgot CHPL Account Password")
                .paragraph("", "We have received a password change request for your CHPL account.")
                .paragraph("", String.format("Click <a href='%s/#/forgot_password/%s'>CHPL</a> here to change your password.  This link is valid for 1 hour.",
                        chplUrl, forgotPassword.getToken().toString()))
                .paragraph("", "If you didn't request a password change, you can ignore this message and continue to use your current password.")
                .footer(PublicFooter.class)
                .build();

        chplEmailFactory.emailBuilder()
            .recipients(List.of(forgotPassword.getEmail()))
            .subject("Request to change CHPL password")
            .htmlMessage(htmlMessage)
            .sendEmail();
    }

}
