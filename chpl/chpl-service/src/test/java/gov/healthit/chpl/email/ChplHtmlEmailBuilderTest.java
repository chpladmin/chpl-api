package gov.healthit.chpl.email;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class ChplHtmlEmailBuilderTest {
    private ChplHtmlEmailBuilder emailBuilder;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() throws IOException {
        Resource htmlSkeletonResource = new ByteArrayResource("<html>${email-content}</html>".getBytes());
        Resource htmlHeadingResource = new ByteArrayResource("<div>${title}</div><div>${subtitle}</div>".getBytes());
        Resource htmlParagraphResource = new ByteArrayResource("<div>${paragraph-heading}</div><div>${paragraph-text}</div>".getBytes());
        Resource htmlTableResource = new ByteArrayResource("<table>${table-header}${table-data}</table>".getBytes());
        Resource htmlButtonBarResource = new ByteArrayResource("<div>${buttons}</div>".getBytes());
        Resource htmlFooterResource = new ByteArrayResource("<div>Footer</div>".getBytes());

       emailBuilder = new ChplHtmlEmailBuilder(htmlSkeletonResource, htmlHeadingResource, htmlParagraphResource,
               htmlTableResource, htmlButtonBarResource, htmlFooterResource);
    }

    @Test
    public void testNewChplHtmlEmailBuilderHasSkeleton_hasCorrectBuiltHtml() {
        assertNotNull(emailBuilder);
        String html = emailBuilder.build();
        assertEquals("<html></html>", html);
    }

    @Test
    public void testEmailWithHeaderOnly_hasCorrectBuiltHtml() {
        assertNotNull(emailBuilder);
        String html = emailBuilder
                .heading("my title", "my subtitle")
                .build();
        assertEquals("<html><div>my title</div><div>my subtitle</div></html>", html);
    }
}
