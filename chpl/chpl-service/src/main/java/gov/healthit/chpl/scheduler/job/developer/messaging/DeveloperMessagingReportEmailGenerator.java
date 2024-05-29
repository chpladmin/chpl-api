package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "messageDevelopersJobLogger")
public class DeveloperMessagingReportEmailGenerator {
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private String emailSubject;
    private String emailSalutation;
    private String emailParagraph;

    private List<String> tableHeaders = List.of("Developer", "Users");

    @Autowired
    public DeveloperMessagingReportEmailGenerator(ChplHtmlEmailBuilder htmlEmailBuilder,
            @Value("${developer.messaging.report.subject}") String emailSubject,
            @Value("${developer.messaging.report.salutation}") String emailSalutation,
            @Value("${developer.messaging.report.paragraph}") String emailParagraph) {
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.emailSubject = emailSubject;
        this.emailSalutation = emailSalutation;
        this.emailParagraph = emailParagraph;
    }

    public MessagingReportEmail getStatusReportEmail(List<DeveloperEmail> developerEmails, String developerMessageSubject, User submittedUser) {
        return MessagingReportEmail.builder()
                .subject(String.format(emailSubject, developerMessageSubject))
                .message(getMessage(developerEmails, developerMessageSubject, submittedUser))
                .recipients(List.of(submittedUser.getEmail()))
                .build();
    }

    private String getMessage(List<DeveloperEmail> developerEmails, String developerMessageSubject, User submittedUser) {
        return htmlEmailBuilder.initialize()
                .heading(String.format(emailSubject, developerMessageSubject))
                .paragraph("", String.format(emailSalutation, submittedUser.getFullName()))
                .paragraph("", emailParagraph)
                .table(tableHeaders, getTableRows(developerEmails))
                .footer(PublicFooter.class)
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
