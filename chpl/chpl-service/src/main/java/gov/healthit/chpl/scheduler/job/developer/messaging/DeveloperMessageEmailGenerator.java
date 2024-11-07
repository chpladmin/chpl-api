package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.messaging.DeveloperMessageRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "messageDevelopersJobLogger")
public class DeveloperMessageEmailGenerator {
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    @Autowired
    public DeveloperMessageEmailGenerator(ChplHtmlEmailBuilder htmlEmailBuilder) {
        this.htmlEmailBuilder = htmlEmailBuilder;
    }

    public DeveloperEmail getDeveloperEmail(DeveloperSearchResult developer,
            DeveloperMessageRequest developerMessageRequest,
            List<User> allDeveloperUsers) {
        List<User> enabledUsersForDeveloper = allDeveloperUsers.stream()
                .filter(user -> user.getOrganizations().stream().map(org -> org.getId()).toList()
                        .contains(developer.getId()))
                .filter(user -> BooleanUtils.isTrue(user.getAccountEnabled()))
                .toList();

        try {
            return DeveloperEmail.builder()
                    .developer(developer)
                    .recipients(getRecipients(enabledUsersForDeveloper))
                    .subject(developerMessageRequest.getSubject())
                    .message(getMessage(developer, enabledUsersForDeveloper, developerMessageRequest))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Could not generate email for Developer Id: {}", developer.getId());
            LOGGER.error(e);
            return null;
        }
    }

    private List<String> getRecipients(List<User> enabledUsersForDeveloper) {
        return enabledUsersForDeveloper.stream()
                    .map(user -> user.getEmail())
                    .toList();
    }

    private String getMessage(DeveloperSearchResult developer, List<User> enabledUsersForDeveloper,
            DeveloperMessageRequest developerMessageRequest) {
        String userEnteredBody = developerMessageRequest.getBody();
        userEnteredBody = replaceTokens(developer, enabledUsersForDeveloper, userEnteredBody);

        return htmlEmailBuilder.initialize()
                .heading(developerMessageRequest.getSubject())
                .paragraph("", convertMarkdownToHtml(userEnteredBody))
                .footer(PublicFooter.class)
                .build();
    }

    private String replaceTokens(DeveloperSearchResult developer, List<User> developerUsers, String text) {
        List<String> devNameTokens = DeveloperMessagingTokens.NAME.getTokenRepresentations();
        for (String devNameToken : devNameTokens) {
            text = text.replace(devNameToken, developer.getName());
        }

        List<String> devUserTokens = DeveloperMessagingTokens.USERS.getTokenRepresentations();
        for (String devUserToken : devUserTokens) {
            text = text.replace(devUserToken, getUsersAsString(developerUsers));
        }
        return text;
    }

    private String getUsersAsString(List<User> developerUsers) {
        List<String> users = developerUsers.stream()
                .map(user -> user.getFullName() + " &lt;" + user.getEmail() + "&gt;")
                .toList();
        return Util.joinListGrammatically(users);
    }

    private String convertMarkdownToHtml(String toConvert) {
        Parser parser  = Parser.builder().build();
        Node document = parser.parse(toConvert);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String converted = renderer.render(document);
        return converted;
    }
}
