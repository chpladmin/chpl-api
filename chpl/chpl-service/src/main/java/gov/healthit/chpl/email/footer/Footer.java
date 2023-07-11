package gov.healthit.chpl.email.footer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

public abstract class Footer {
    private String htmlFooter;

    public Footer(Resource htmlFooterResource) throws IOException {
        htmlFooter = StreamUtils.copyToString(htmlFooterResource.getInputStream(), StandardCharsets.UTF_8);
    }

    public String getFooterHtml() {
        return htmlFooter;
    }

    public abstract String build();
}