package gov.healthit.chpl.changerequest.domain.service.email;

import java.text.DateFormat;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;

@Component
public class AttestationRejectedEmail extends ChangeRequestEmail {
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String emailSubject;
    private String emailBody;


    @Autowired
    public AttestationRejectedEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDeveloperMapDAO userDeveloperMapDAO,
            @Value("${changeRequest.attestation.rejected.subject}") String emailSubject,
            @Value("${changeRequest.attestation.rejected.body}") String emailBody) {

        super(userDeveloperMapDAO);
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
        .htmlMessage(createRejectedHtmlMessage(cr))
        .sendEmail();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Rejected")
                .paragraph("", String.format(emailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr), cr.getCurrentStatus().getComment()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

}
