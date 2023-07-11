package gov.healthit.chpl.email.footer;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

public class AdminFooter extends Footer {
    private String feedbackUrlTag;
    private String additionalContentTag;
    private String adminAcbAndAtlFeedbackUrl;

    public AdminFooter(Resource htmlFooterResource,
            String feedbackUrlTag,
            String additionalContentTag,
            Environment env) throws IOException {
        super(htmlFooterResource);
        this.adminAcbAndAtlFeedbackUrl = env.getProperty("contact.acbatlUrl");
        this.feedbackUrlTag = feedbackUrlTag;
        this.additionalContentTag = additionalContentTag;
    }

    public String build() {
        String modifiedHtmlFooter = new String(getFooterHtml());
        modifiedHtmlFooter = modifiedHtmlFooter.replace(feedbackUrlTag, adminAcbAndAtlFeedbackUrl);
        modifiedHtmlFooter = modifiedHtmlFooter.replace(additionalContentTag, "");
        return modifiedHtmlFooter;
    }
}