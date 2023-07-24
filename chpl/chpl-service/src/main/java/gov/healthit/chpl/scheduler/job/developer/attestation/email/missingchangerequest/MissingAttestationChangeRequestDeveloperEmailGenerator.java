package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmailGenerator;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "missingAttestationChangeRequestEmailJobLogger")
public class MissingAttestationChangeRequestDeveloperEmailGenerator implements DeveloperEmailGenerator {
    private DeveloperManager developerManager;
    private ChplHtmlEmailBuilder htmlEmailBuilder;
    private String emailSubject;
    private String emailSalutation;
    private String emailParagraph1;
    private String emailParagraph2;
    private String emailParagraph3;
    private String emailClosing;

    @Autowired
    public MissingAttestationChangeRequestDeveloperEmailGenerator(DeveloperManager developerManager, ChplHtmlEmailBuilder htmlEmailBuilder,
            @Value("${developer.missingAttestationChangeRequest.subject}") String emailSubject,
            @Value("${developer.missingAttestationChangeRequest.salutation}") String emailSalutation,
            @Value("${developer.missingAttestationChangeRequest.paragraph1}") String emailParagraph1,
            @Value("${developer.missingAttestationChangeRequest.paragraph2}") String emailParagraph2,
            @Value("${developer.missingAttestationChangeRequest.paragraph3}") String emailParagraph3,
            @Value("${developer.missingAttestationChangeRequest.closing}") String emailClosing) {
        this.developerManager = developerManager;
        this.htmlEmailBuilder = htmlEmailBuilder;
        this.emailSubject = emailSubject;
        this.emailSalutation = emailSalutation;
        this.emailParagraph1 = emailParagraph1;
        this.emailParagraph2 = emailParagraph2;
        this.emailParagraph3 = emailParagraph3;
        this.emailClosing = emailClosing;
    }


    @Override
    public DeveloperEmail getDeveloperEmail(Developer developer) {
        try {
            List<UserDTO> developerUsers = developerManager.getAllUsersOnDeveloper(developer.getId());
            return DeveloperEmail.builder()
                    .developer(developer)
                    .recipients(getRecipients(developerUsers))
                    .subject(emailSubject)
                    .message(getMessage(developer, developerUsers))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Could not generate email for Developer Id: {}", developer.getId());
            LOGGER.error(e);
            return null;
        }
    }

    private List<String> getRecipients(List<UserDTO> developerUsers) {
        return developerUsers.stream()
                    .map(user -> user.getEmail())
                    .toList();
    }

    private String getMessage(Developer developer, List<UserDTO> developerUsers) {
        return htmlEmailBuilder.initialize()
                .heading(emailSubject)
                .paragraph("", emailSalutation)
                .paragraph("", String.format(emailParagraph1, developer.getName()))
                .paragraph("", String.format(emailParagraph2, getUsersAsString(developerUsers)))
                .paragraph("", emailParagraph3)
                .paragraph("", emailClosing)
                .footer(PublicFooter.class)
                .build();
    }

    private String getUsersAsString(List<UserDTO> developerUsers) {
        List<String> users = developerUsers.stream()
                .map(user -> user.getFullName() + " &lt;" + user.getEmail() + "&gt;")
                .toList();
        return Util.joinListGrammatically(users);
    }
}
