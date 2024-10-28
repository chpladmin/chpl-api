package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "messageDevelopersJobLogger")
public class DeveloperMessagePreviewEmailService {
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private String developerUrlUnformatted;
    private String missingActiveUsersMessage;

    @Autowired
    public DeveloperMessagePreviewEmailService(ChplHtmlEmailBuilder chplHtmlEmailBuilder,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${developerUrlPart}") String developerUrlPart,
            @Value("${developer.messaging.missingActiveUsers}") String missingActiveUsersMessage) {
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.developerUrlUnformatted = chplUrlBegin + developerUrlPart;
        this.missingActiveUsersMessage = missingActiveUsersMessage;
    }

    public String prependPreviewNotice(String htmlBody, DeveloperEmail developer) {
        StringBuffer message = new StringBuffer();
        message.append("<b>");
        message.append("This is a preview of what the developer " + developer.getDeveloper().getName() + " will receive.");
        message.append("</b>");
        message.append("<br/><br/>");
        message.append(htmlBody);
        return message.toString();
    }

    public String appendMissingUsers(String htmlBody, List<DeveloperSearchResult> developersWithoutUsers) {
        if (CollectionUtils.isEmpty(developersWithoutUsers)) {
            return htmlBody;
        }
        developersWithoutUsers = developersWithoutUsers.stream()
            .sorted(new Comparator<DeveloperSearchResult>() {

                @Override
                public int compare(DeveloperSearchResult o1, DeveloperSearchResult o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            })
            .toList();

        StringBuffer devsHtml = new StringBuffer();
        devsHtml.append("<b>" + formatMissingActiveUsersMessage(developersWithoutUsers) + "</b>");
        devsHtml.append("<ul>");
        developersWithoutUsers.stream()
            .forEach(dev -> devsHtml.append("<li><a href=\"" + String.format(developerUrlUnformatted, dev.getId() + "") + "\">"
                    + dev.getName() + "</a></li>"));
        devsHtml.append("</ul>");

        StringBuffer message = new StringBuffer();
        message.append(htmlBody);
        message.append(chplHtmlEmailBuilder.getParagraphHtml(null, devsHtml.toString(), null, "#f5f9fd"));
        return message.toString();
    }

    private String formatMissingActiveUsersMessage(List<DeveloperSearchResult> developersWithoutUsers) {
        return String.format(missingActiveUsersMessage,
                developersWithoutUsers.size() > 1 ? ("These " + developersWithoutUsers.size()) : "This",
                developersWithoutUsers.size() > 1 ? "s" : "",
                developersWithoutUsers.size() > 1 ? "ve" : "s");
    }
}
