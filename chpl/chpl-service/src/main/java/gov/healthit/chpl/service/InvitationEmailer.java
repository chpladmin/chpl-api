package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InvitationEmailer {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private String chplUrlBegin;
    private String chplEmailValediction;

    private String accountInvitationTitle;
    private String accountInvitationHeading;
    private String accountInvitationParagraph1;
    private String accountInvitationParagraph2;
    private String accountInvitationLink;

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
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${contact.publicUrl}") String publicFeedbackUrl,
            @Value("${chpl.email.valediction}") String chplEmailValediction) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.chplEmailFactory = chplEmailFactory;

        this.accountInvitationTitle = accountInvitationTitle;
        this.accountInvitationHeading = accountInvitationHeading;
        this.accountInvitationParagraph1 = accountInvitationParagraph1;
        this.accountInvitationParagraph2 = String.format(accountInvitationParagraph2, invitationLengthDays);
        this.accountInvitationLink = accountInvitationLink;

        this.chplUrlBegin = chplUrlBegin;
        this.chplEmailValediction = String.format(chplEmailValediction, publicFeedbackUrl);
    }

    public void emailInvitedUser(CognitoUserInvitation invitation) {
        String htmlMessage = htmlEmailBuilder.initialize()
                .heading(accountInvitationTitle)
                .paragraph(accountInvitationHeading, accountInvitationParagraph1)
                .paragraph(null, String.format(accountInvitationLink, chplUrlBegin, invitation.getInvitationToken()))
                .paragraph(null, accountInvitationParagraph2)
                .paragraph(null, chplEmailValediction)
                .footer(PublicFooter.class)
                .build();
        String[] toEmails = {
                invitation.getEmail()
        };
        LOGGER.info("Created HTML Message for " + invitation.getEmail());
        try {
            LOGGER.info("Created new email builder");
            chplEmailFactory.emailBuilder().recipients(new ArrayList<String>(Arrays.asList(toEmails)))
                .subject(accountInvitationTitle)
                .htmlMessage(htmlMessage)
                .sendEmail();
            LOGGER.info("Sent email to " + invitation.getEmail());
        } catch (EmailNotSentException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
