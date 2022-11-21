package gov.healthit.chpl.scheduler.job.developer.attestation.email.missingchangerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmail;
import gov.healthit.chpl.scheduler.job.developer.attestation.email.DeveloperEmailGenerator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MissingAttestationChangeRequestDeveloperEmailGenerator implements DeveloperEmailGenerator {
    private DeveloperManager developerManager;
    private String emailSubject;
    private String emailBody;

    @Autowired
    public MissingAttestationChangeRequestDeveloperEmailGenerator(DeveloperManager developerManager,
            @Value("${developer.missingAttestationChangeRequest.subject}") String emailSubject,
            @Value("${developer.missingAttestationChangeRequest.body}") String emailBody) {
        this.developerManager = developerManager;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
    }


    @Override
    public DeveloperEmail getDeveloperEmail(Developer developer) {
        try {
            List<UserDTO> developerUsers = developerManager.getAllUsersOnDeveloper(developer.getId());
            return DeveloperEmail.builder()
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
        return String.format(emailBody, developer.getName(), getUsersAsString(developerUsers));
    }

    private String getUsersAsString(List<UserDTO> developerUsers) {
        return developerUsers.stream()
                .map(user -> user.getFullName() + " <" + user.getEmail() + ">")
                .collect(Collectors.joining(", "));
    }
}
