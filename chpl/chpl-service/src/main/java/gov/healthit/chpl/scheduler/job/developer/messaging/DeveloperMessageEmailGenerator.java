package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "messageDevelopersJobLogger")
public class DeveloperMessageEmailGenerator {
    private DeveloperManager developerManager;
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    @Autowired
    public DeveloperMessageEmailGenerator(DeveloperManager developerManager,
            ChplHtmlEmailBuilder htmlEmailBuilder) {
        this.developerManager = developerManager;
        this.htmlEmailBuilder = htmlEmailBuilder;
    }

    public DeveloperEmail getDeveloperEmail(DeveloperSearchResult developer) {
        try {
            List<User> developerUsers = developerManager.getAllUsersOnDeveloper(developer.getId());
            return DeveloperEmail.builder()
                    .developer(developer)
//                    .recipients(getRecipients(developerUsers))
//                    .subject(emailSubject)
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

    private String getMessage(DeveloperSearchResult developer, List<User> developerUsers) {
        return htmlEmailBuilder.initialize()
//                .heading(emailSubject)
//                .paragraph("", emailSalutation)
//                .paragraph("", String.format(emailParagraph1, developer.getName()))
//                .paragraph("", String.format(emailParagraph2, getUsersAsString(developerUsers)))
//                .paragraph("", emailParagraph3)
//                .paragraph("", emailClosing)
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
