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
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;

@Component
public class AttestationUpdatedEmail extends ChangeRequestEmail {
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String emailSubject;
    private String emailBody;


    @Autowired
    public AttestationUpdatedEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDeveloperMapDAO userDeveloperMapDAO,
            @Value("${changeRequest.attestation.updated.subject}") String emailSubject,
            @Value("${changeRequest.attestation.updated.body}") String emailBody) {

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
            .htmlMessage(createUpdatedHtmlMessage(cr))
            .sendEmail();
    }

    private String createUpdatedHtmlMessage(ChangeRequest cr) {
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) cr.getDetails();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        String period = formatter.format(details.getAttestationPeriod().getPeriodStart()) + " - " + formatter.format(details.getAttestationPeriod().getPeriodEnd());
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Submitted")
                .paragraph("", String.format(emailBody, cr.getDeveloper().getName(), period))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(PublicFooter.class)
                .build();
    }
}
