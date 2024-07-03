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

    public void sendEmail(String email, String tempPassword) throws EmailNotSentException {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading("Forgot CHPL Account Password")
                .paragraph("", "We have received a password change request for your CHPL account.")
                .paragraph("", String.format("Please go to the <a href='%s'>CHPL</a> and login with this one-time password. You "
                        + "will be reuqired to change your password after successfully logging in.", chplUrl))
                .paragraph("", String.format("One-time Password: %s", tempPassword))
                .paragraph("", "For security reasons your one-time password is only valid for 7 days.")
                .paragraph("", "If you didn't ask to reset your password, you can disregard this email.")
                .footer(PublicFooter.class)
                .build();

        chplEmailFactory.emailBuilder()
            .recipients(List.of(email))
            .subject("CHPL Forgot Password")
            .htmlMessage(htmlMessage)
            .sendEmail();
    }

}
