package gov.healthit.chpl.email.footer;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

public class PublicFooter extends Footer {
    private String feedbackUrlTag;
    private String additionalContentTag;
    private String url;

    public PublicFooter(Resource htmlFooterResource,
            String feedbackUrlTag,
            String additionalContentTag,
            Environment env) throws IOException {
        super(htmlFooterResource);
        this.url = env.getProperty("contact.publicUrl");
        this.feedbackUrlTag = feedbackUrlTag;
        this.additionalContentTag = additionalContentTag;
    }

    public String build() {
        String modifiedHtmlFooter = new String(getFooterHtml());
        modifiedHtmlFooter = modifiedHtmlFooter.replace(feedbackUrlTag, url);
        modifiedHtmlFooter = modifiedHtmlFooter.replace(additionalContentTag, "");
        return modifiedHtmlFooter;
    }
}
