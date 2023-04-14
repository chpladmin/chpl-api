package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserAccountUpdateEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private ChplEmailFactory chplEmailFactory;

    private String chplUrlBegin;
    private String chplEmailGreeting;
    private String chplEmailValediction;

    private String passwordResetEmailSubject;
    private String passwordResetEmailBody;
    private String passwordResetEmailLink;

    @Autowired
    public UserAccountUpdateEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory,
            @Value("${user.resetPassword.subject}") String passwordResetEmailSubject,
            @Value("${user.resetPassword.body}") String passwordResetEmailBody,
            @Value("${user.resetPassword.resetLink}") String passwordResetEmailLink,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${footer.publicUrl}") String publicFeedbackUrl,
            @Value("${chpl.email.greeting}") String chplEmailGreeting,
            @Value("${chpl.email.valediction}") String chplEmailValediction) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;

        this.passwordResetEmailSubject = passwordResetEmailSubject;
        this.passwordResetEmailBody = passwordResetEmailBody;
        this.passwordResetEmailLink = passwordResetEmailLink;

        this.chplUrlBegin = chplUrlBegin;
        this.chplEmailGreeting = chplEmailGreeting;
        this.chplEmailValediction = String.format(chplEmailValediction, publicFeedbackUrl);
    }

    public void sendPasswordResetEmail(String token, String userEmail) {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(passwordResetEmailSubject)
                .paragraph(null, passwordResetEmailBody)
                .paragraph(null, String.format(passwordResetEmailLink, chplUrlBegin, token))
                .paragraph(null, chplEmailValediction)
                .footer(true)
                .build();
        String[] toEmails = {
                userEmail
        };
        LOGGER.info("Created HTML Message to reset password for " + userEmail);
        try {
            chplEmailFactory.emailBuilder().recipients(new ArrayList<String>(Arrays.asList(toEmails)))
                .subject(passwordResetEmailSubject)
                .htmlMessage(htmlMessage)
                .sendEmail();
            LOGGER.info("Sent email to " + userEmail);
        } catch (EmailNotSentException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
