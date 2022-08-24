package gov.healthit.chpl.changerequest.domain.service.email;

import java.time.format.DateTimeFormatter;
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
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;

@Component
public class AttestationWithdrawnEmail extends ChangeRequestEmail {
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String emailSubject;
    private String emailBody;


    @Autowired
    public AttestationWithdrawnEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDeveloperMapDAO userDeveloperMapDAO,
            @Value("${changeRequest.attestation.withdrawn.subject}") String emailSubject,
            @Value("${changeRequest.attestation.withdrawn.body}") String emailBody) {

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
            .htmlMessage(withdrawnUpdatedHtmlMessage(cr))
            .sendEmail();
    }

    private String withdrawnUpdatedHtmlMessage(ChangeRequest cr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Withdrawn")
                .paragraph("", String.format(emailBody,
                        cr.getDeveloper().getName(),
                        formatter.format(DateUtil.toLocalDate(cr.getSubmittedDate().getTime())),
                        AuthUtil.getUsername()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }
}
