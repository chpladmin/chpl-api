package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
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
    private String accountInvitationParagraph1;
    private String accountInvitationParagraph2;
    private String accountInvitationLink;

    private String accountConfirmationTitle;
    private String accountConfirmationBody;
    private String accountConfirmationLink;
    private ChplEmailFactory chplEmailFactory;


    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public InvitationEmailer(ChplHtmlEmailBuilder htmlEmailBuilder, ChplEmailFactory chplEmailFactory,
            @Value("${account.invitation.title}") String accountInvitationTitle,
            @Value("${account.invitation.heading}") String accountInvitationHeading,
            @Value("${account.invitation.paragraph1}") String accountInvitationParagraph1,
            @Value("${account.invitation.paragraph2}") String accountInvitationParagraph2,
            @Value("${account.invitation.invitationLink}") String accountInvitationLink,
            @Value("${invitationLengthInDays}") Long invitationLengthDays,
            @Value("${account.conirmation.title}") String accountConfirmationTitle,
            @Value("${account.confirmation.body}") String accountConfirmationBody,
            @Value("${account.confirmation.confirmationLink}") String accountConfirmationLink,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${contact.publicUrl}") String publicFeedbackUrl,
            @Value("${chpl.email.greeting}") String chplEmailGreeting,
            @Value("${chpl.email.valediction}") String chplEmailValediction) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;

        this.accountInvitationTitle = accountInvitationTitle;
        this.accountInvitationHeading = accountInvitationHeading;
        this.accountInvitationParagraph1 = accountInvitationParagraph1;
        this.accountInvitationParagraph2 = String.format(accountInvitationParagraph2, invitationLengthDays);
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
                .paragraph(accountInvitationHeading, accountInvitationParagraph1)
                .paragraph(null, String.format(accountInvitationLink, chplUrlBegin, invitation.getInvitationToken()))
                .paragraph(null, accountInvitationParagraph2)
                .paragraph(null, chplEmailValediction)
                .footer(true)
                .build();
        String[] toEmails = {
                invitation.getEmailAddress()
        };
        LOGGER.info("Created HTML Message for " + invitation.getEmailAddress());
        try {
            LOGGER.info("Created new email builder");
            chplEmailFactory.emailBuilder().recipients(new ArrayList<String>(Arrays.asList(toEmails)))
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
            chplEmailFactory.emailBuilder()
                    .recipients(new ArrayList<String>(Arrays.asList(toEmails)))
                    .subject(accountConfirmationTitle)
                    .htmlMessage(htmlMessage)
                    .sendEmail();
        } catch (EmailNotSentException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
