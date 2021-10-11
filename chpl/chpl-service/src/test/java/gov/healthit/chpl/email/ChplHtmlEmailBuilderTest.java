package gov.healthit.chpl.email;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Resource htmlHeadingResource = new ByteArrayResource("<div>${title}</div>".getBytes());
        Resource htmlParagraphResource = new ByteArrayResource("<div>${paragraph-heading}</div><div>${paragraph-text}</div>".getBytes());
        Resource htmlTableResource = new ByteArrayResource("<table>${table-header}${table-data}</table>".getBytes());
        Resource htmlButtonBarResource = new ByteArrayResource("<div>${buttons}</div>".getBytes());
        Resource htmlFooterResource = new ByteArrayResource("<div>${feedback-url}</div>".getBytes());

       emailBuilder = new ChplHtmlEmailBuilder(htmlSkeletonResource, htmlHeadingResource, htmlParagraphResource,
               htmlTableResource, htmlButtonBarResource, htmlFooterResource,
               "http://www.adminUrl.com", "http://www.publicUrl.com");

       assertNotNull(emailBuilder);
    }

    @Test
    public void testNewChplHtmlEmailBuilderHasSkeleton_hasExpectedHtml() {
        String html = emailBuilder.build();
        assertEquals("<html></html>", html);
    }

    @Test
    public void testEmailWithHeaderOnly_hasExpectedHtml() {
        String html = emailBuilder
                .heading("my title")
                .build();
        assertEquals("<html><div><h1>my title</h1></div></html>", html);
    }

    @Test
    public void testEmailWithParagraphOnly_hasExpectedHtml() {
        String html = emailBuilder
                .paragraph("paragraph heading", "some text")
                .build();
        assertEquals("<html><div><h2>paragraph heading</h2></div><div><p>some text</p></div></html>", html);
    }

    @Test
    public void testEmailWithParagraphWithHeadingOnly_hasExpectedHtml() {
        String html = emailBuilder
                .paragraph("paragraph heading", "")
                .build();
        assertEquals("<html><div><h2>paragraph heading</h2></div><div></div></html>", html);
    }

    @Test
    public void testEmailWithParagraphWithContentOnly_hasExpectedHtml() {
        String html = emailBuilder
                .paragraph("", "some text")
                .build();
        assertEquals("<html><div></div><div><p>some text</p></div></html>", html);
    }

    @Test
    public void testEmailWithTableOnly_hasExpectedHtml() {
        List<String> headings = Stream.of("Col1", "Col2", "Col3").collect(Collectors.toList());
        List<List<String>> data = Stream.of(
                Stream.of("11", "12", "13").collect(Collectors.toList()),
                Stream.of("21", "22", "23").collect(Collectors.toList()),
                Stream.of("31", "32", "33").collect(Collectors.toList())).collect(Collectors.toList());
        String html = emailBuilder
                .table(headings, data)
                .build();
        assertEquals("<html><table>"
                + "<tr><th align=\"left\">Col1</th><th align=\"left\">Col2</th><th align=\"left\">Col3</th></tr>"
                + "<tr><td>11</td><td>12</td><td>13</td></tr>"
                + "<tr><td>21</td><td>22</td><td>23</td></tr>"
                + "<tr><td>31</td><td>32</td><td>33</td></tr>"
                + "</table></html>", html);
    }

    @Test
    public void testEmailWithTableEmptyData_hasExpectedHtml() {
        List<String> headings = Stream.of("Col1", "Col2", "Col3").collect(Collectors.toList());
        List<List<String>> data = Collections.EMPTY_LIST;

        String html = emailBuilder
                .table(headings, data)
                .build();
        assertEquals("<html><table>"
                + "<tr><th align=\"left\">Col1</th><th align=\"left\">Col2</th><th align=\"left\">Col3</th></tr>"
                + "<tr><td colspan=\"3\">No Applicable Data</td></tr>"
                + "</table></html>", html);
    }

    @Test
    public void testEmailWithPublicFooterOnly_hasExpectedHtml() {
        String html = emailBuilder
                .footer(true)
                .build();
        assertEquals("<html><div>http://www.publicUrl.com</div></html>", html);
    }

    @Test
    public void testEmailWithAdminFooterOnly_hasExpectedHtml() {
        String html = emailBuilder
                .footer(false)
                .build();
        assertEquals("<html><div>http://www.adminUrl.com</div></html>", html);
    }

    @Test
    public void testEmailWithHeaderTitleAndParagraphText_hasExpectedHtml() {
        String html = emailBuilder
                .heading("my title")
                .paragraph("",  "some text")
                .build();
        assertEquals("<html><div><h1>my title</h1></div><div></div><div><p>some text</p></div></html>", html);
    }

    @Test
    public void testEmailWithHeaderTitleAndParagraphAndFooterText_hasExpectedHtml() {
        String html = emailBuilder
                .heading("my title")
                .paragraph("",  "some text")
                .footer(true)
                .build();
        assertEquals("<html><div><h1>my title</h1></div><div></div><div><p>some text</p></div><div>http://www.publicUrl.com</div></html>", html);
    }

    @Test
    public void testEmailWithTwoParagraphsAndFooterText_hasExpectedHtml() {
        String html = emailBuilder
                .paragraph("my title", "p1 text")
                .paragraph("",  "some text")
                .footer(true)
                .build();
        assertEquals("<html><div><h2>my title</h2></div><div><p>p1 text</p></div><div></div><div><p>some text</p></div><div>http://www.publicUrl.com</div></html>", html);
    }
}
