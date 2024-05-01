package gov.healthit.chpl.changerequest.domain.service.email;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

@Component
public class AttestationPendingDeveloperActionEmail extends ChangeRequestEmail {
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String emailSubject;
    private String emailBody;


    @Autowired
    public AttestationPendingDeveloperActionEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDeveloperMapDAO userDeveloperMapDAO,
            ResourcePermissionsFactory resourcePermissionsFactory, FF4j ff4j,
            @Value("${changeRequest.attestation.pendingDeveloperAction.subject}") String emailSubject,
            @Value("${changeRequest.attestation.pendingDeveloperAction.body}") String emailBody) {

        super(userDeveloperMapDAO, resourcePermissionsFactory, ff4j);
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
    }

    @Override
    public void send(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
        .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                .map(user -> user.getEmail())
                .collect(Collectors.toList()))
        .subject(emailSubject)
        .htmlMessage(createPendingDeveloperActionHtmlMessage(cr))
        .sendEmail();
    }

    private String createPendingDeveloperActionHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Action Required")
                .paragraph("", String.format(emailBody,
                        cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(PublicFooter.class)
                .build();
    }

}
