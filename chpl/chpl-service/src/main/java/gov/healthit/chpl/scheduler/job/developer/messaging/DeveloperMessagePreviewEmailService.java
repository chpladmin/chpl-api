package gov.healthit.chpl.scheduler.job.developer.messaging;

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

    @Autowired
    public DeveloperMessagePreviewEmailService(ChplHtmlEmailBuilder chplHtmlEmailBuilder,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.developerUrlUnformatted = chplUrlBegin + developerUrlPart;
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

        StringBuffer devsHtml = new StringBuffer();
        devsHtml.append("<b>The following developers do not have active users and will not receive the messsage: </b>");
        devsHtml.append("<ul>");
        developersWithoutUsers.stream()
            .forEach(dev -> devsHtml.append("<li><a href=\"" + String.format(developerUrlUnformatted, dev.getId() + "") + "\">"
                    + dev.getName() + "</a></li>"));
        devsHtml.append("</ul>");

        StringBuffer message = new StringBuffer();
        message.append(htmlBody);
        message.append(chplHtmlEmailBuilder.getParagraphHtml(null, devsHtml.toString(), null, "#fdfde7"));
        return message.toString();
    }
}
