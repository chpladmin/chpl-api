package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.StatusReportEmail;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "missingAttestationChangeRequestEmailJobLogger")
public class MissingAttestationChangeRequestDeveloperStatusReportEmailGenerator {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private String emailSubject;
    private String emailSalutation;
    private String emailParagraph;

    private List<String> tableHeaders = List.of("Developer", "Users");

    @Autowired
    public MissingAttestationChangeRequestDeveloperStatusReportEmailGenerator(ChplHtmlEmailBuilder htmlEmailBuilder,
            @Value("${developer.missingAttestationChangeRequest.report.subject}") String emailSubject,
            @Value("${developer.missingAttestationChangeRequest.report.salutation}") String emailSalutation,
            @Value("${developer.missingAttestationChangeRequest.report.paragraph}") String emailParagraph) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.emailSubject = emailSubject;
        this.emailSalutation = emailSalutation;
        this.emailParagraph = emailParagraph;
    }

    public StatusReportEmail getStatusReportEmail(List<DeveloperEmail> developerEmails, UserDTO submittedUser) {
        return StatusReportEmail.builder()
                .subject(emailSubject)
                .message(getMessage(developerEmails, submittedUser))
                .recipients(List.of(submittedUser.getEmail()))
                .build();
    }

    private String getMessage(List<DeveloperEmail> developerEmails, UserDTO submittedUser) {
        return htmlEmailBuilder.initialize()
                .heading(emailSubject)
                .paragraph("", String.format(emailSalutation, submittedUser.getFullName()))
                .paragraph("", emailParagraph)
                .table(tableHeaders, getTableRows(developerEmails))
                .footer(true)
                .build();
    }

    private List<List<String>> getTableRows(List<DeveloperEmail> developerEmails) {
        if (developerEmails.size() > 0) {
            return developerEmails.stream()
                    .map(email -> List.of(email.getDeveloper().getName(),
                            email.getRecipients().size() == 0 ? "No users found" : email.getRecipients().stream().collect(Collectors.joining("; "))))
                    .toList();
        } else {
            return List.of(List.of("No Emails Sent"));
        }
    }

}
