package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InvitationEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private String chplUrlBegin;
    private String chplEmailGreeting;
    private String chplEmailValediction;

    private String accountInvitationTitle;
    private String accountInvitationHeading;
    private String accountInvitationBody;
    private String accountInvitationLink;

    private String accountConfirmationTitle;
    private String accountConfirmationBody;
    private String accountConfirmationLink;
    private Environment env;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public InvitationEmailer(ChplHtmlEmailBuilder htmlEmailBuilder,
            Environment env,
            @Value("${account.invitation.title}") String accountInvitationTitle,
            @Value("${account.invitation.heading}") String accountInvitationHeading,
            @Value("${account.invitation.body}") String accountInvitationBody,
            @Value("${account.invitation.invitationLink}") String accountInvitationLink,
            @Value("${account.conirmation.title}") String accountConfirmationTitle,
            @Value("${account.confirmation.body}") String accountConfirmationBody,
            @Value("${account.confirmation.confirmationLink}") String accountConfirmationLink,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${footer.publicUrl}") String publicFeedbackUrl,
            @Value("${chpl.email.greeting}") String chplEmailGreeting,
            @Value("${chpl.email.valediction}") String chplEmailValediction) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.env = env;

        this.accountInvitationTitle = accountInvitationTitle;
        this.accountInvitationHeading = accountInvitationHeading;
        this.accountInvitationBody = accountInvitationBody;
        this.accountInvitationLink = accountInvitationLink;

        this.accountConfirmationTitle = accountConfirmationTitle;
        this.accountConfirmationBody = accountConfirmationBody;
        this.accountConfirmationLink = accountConfirmationLink;

        this.chplUrlBegin = chplUrlBegin;
        this.chplEmailGreeting = chplEmailGreeting;
        this.chplEmailValediction = String.format(chplEmailValediction, publicFeedbackUrl);
    }

    public void emailInvitedUser(UserInvitation invitation) {
        String htmlMessage = htmlEmailBuilder.initialize()
            .heading(accountInvitationTitle)
            .paragraph(accountInvitationHeading, accountInvitationBody)
            .paragraph(null, String.format(accountInvitationLink, chplUrlBegin, invitation.getInvitationToken()))
            .paragraph(null, chplEmailValediction)
            .footer(true)
            .build();
        String[] toEmails = {
                invitation.getEmailAddress()
        };
        LOGGER.info("Created HTML Message for " + invitation.getEmailAddress());
        try {
            EmailBuilder emailBuilder = new EmailBuilder(env);
            LOGGER.info("Created new email builder");
            emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
                .subject(accountInvitationTitle)
                .htmlMessage(htmlMessage)
                .sendEmail();
            LOGGER.info("Sent email to " + invitation.getEmailAddress());
        } catch (EmailNotSentException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void emailNewUser(UserDTO newUser, UserInvitation invitation) {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(accountConfirmationTitle)
                .paragraph(String.format(chplEmailGreeting, newUser.getFullName()), accountConfirmationBody)
                .paragraph(null, String.format(accountConfirmationLink, chplUrlBegin, invitation.getConfirmationToken()))
                .paragraph(null, chplEmailValediction)
                .footer(true)
                .build();

        String[] toEmails = {
                newUser.getEmail()
        };
        try {
            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
            .subject(accountConfirmationTitle)
            .htmlMessage(htmlMessage)
            .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
