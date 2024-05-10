package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.messaging.DeveloperMessageRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.manager.DeveloperManager;
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

    public DeveloperEmail getDeveloperEmail(DeveloperSearchResult developer, DeveloperMessageRequest developerMessageRequest) {
        try {
            List<User> developerUsers = developerManager.getAllUsersOnDeveloper(developer.getId());
            return DeveloperEmail.builder()
                    .developer(developer)
                    .recipients(getRecipients(developerUsers))
                    .subject(developerMessageRequest.getSubject())
                    .message(getMessage(developer, developerMessageRequest))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Could not generate email for Developer Id: {}", developer.getId());
            LOGGER.error(e);
            return null;
        }
    }

    private List<String> getRecipients(List<User> developerUsers) {
        return developerUsers.stream()
                    .map(user -> user.getEmail())
                    .toList();
    }

    private String getMessage(DeveloperSearchResult developer, DeveloperMessageRequest developerMessageRequest) {
        return htmlEmailBuilder.initialize()
                .heading(developerMessageRequest.getSubject())
                .paragraph("", developerMessageRequest.getBody())
                .footer(PublicFooter.class)
                .build();
    }
}
