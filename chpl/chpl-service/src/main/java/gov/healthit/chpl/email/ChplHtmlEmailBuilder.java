package gov.healthit.chpl.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

public class ChplHtmlEmailBuilder {
    private static final String EMAIL_CONTENT_TAG = "{email-content}";
    private static final String TITLE_TAG = "{title}";
    private static final String SUBTITLE_TAG = "{subtitle}";
    private static final String PARAGRAPH_HEADING_TAG = "{paragraph-heading}";
    private static final String PARAGRAPH_TEXT_TAG = "{paragraph-text}";
    private static final String TABLE_HEADER_TAG = "{table-header}";
    private static final String TABLE_DATA_TAG = "{table-data}";
    private static final String BUTTON_BAR_TAG = "{buttons}";

    private String htmlSkeleton;
    private String htmlHeading;
    private String htmlParagraph;
    private String htmlTable;
    private String htmlButtonBar;
    private String htmlFooter;

    private StringBuilder emailContents;

    public ChplHtmlEmailBuilder() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource htmlSkeletonResource = resourceLoader.getResource("classpath:email/chpl-email-skeleton.html");
        htmlSkeleton = StreamUtils.copyToString(htmlSkeletonResource.getInputStream(), StandardCharsets.UTF_8);
        Resource htmlHeadingResource = resourceLoader.getResource("classpath:email/chpl-email-heading.html");
        htmlHeading = StreamUtils.copyToString(htmlHeadingResource.getInputStream(), StandardCharsets.UTF_8);
        Resource htmlParagraphResource = resourceLoader.getResource("classpath:email/chpl-email-paragraph.html");
        htmlParagraph = StreamUtils.copyToString(htmlParagraphResource.getInputStream(), StandardCharsets.UTF_8);
        Resource htmlTableResource = resourceLoader.getResource("classpath:email/chpl-email-table.html");
        htmlTable = StreamUtils.copyToString(htmlTableResource.getInputStream(), StandardCharsets.UTF_8);
        Resource htmlButtonBarResource = resourceLoader.getResource("classpath:email/chpl-email-button-bar.html");
        htmlButtonBar = StreamUtils.copyToString(htmlButtonBarResource.getInputStream(), StandardCharsets.UTF_8);
        Resource htmlFooterResource = resourceLoader.getResource("classpath:email/chpl-email-footer.html");
        htmlFooter = StreamUtils.copyToString(htmlFooterResource.getInputStream(), StandardCharsets.UTF_8);

        emailContents = new StringBuilder(htmlSkeleton);
    }

    public ChplHtmlEmailBuilder addHeading(String title, String subtitle) {
        if (StringUtils.isAllBlank(title, subtitle)) {
            return this;
        }

        Map<String, String> values = new HashMap<String, String>();
        if (!StringUtils.isEmpty(title)) {
            values.put(TITLE_TAG, "<h1>" + title + "</h1>");
        } else {
            values.put(TITLE_TAG, "");
        }
        if (!StringUtils.isEmpty(subtitle)) {
            values.put(SUBTITLE_TAG, "<p style="
                    + "\"margin-top: -10px; "
                    + "font-size: 12px; "
                    + "color: white; "
                    + "font-family: 'Open Sans', "
                    + "sans-serif; "
                    + "font-weight: 400; "
                    + "padding-bottom: 64px;\">" + subtitle + "</p>");
        } else {
            values.put(SUBTITLE_TAG, "");
        }

        String modifiedHtmlHeading = StringSubstitutor.replace(htmlHeading, values, "{", "}");
        addItemToEmailContents(modifiedHtmlHeading);
        return this;
    }

    public ChplHtmlEmailBuilder addParagraph(String heading, String text) {
        if (StringUtils.isAllBlank(heading, text)) {
            return this;
        }

        Map<String, String> values = new HashMap<String, String>();
        if (!StringUtils.isEmpty(heading)) {
            values.put(PARAGRAPH_HEADING_TAG, "<h2>" + heading + "</h2>");
        } else {
            values.put(PARAGRAPH_HEADING_TAG, "");
        }
        if (!StringUtils.isEmpty(text)) {
            values.put(PARAGRAPH_TEXT_TAG, "<p>" + text + "</p>");
        } else {
            values.put(PARAGRAPH_TEXT_TAG, "");
        }

        String modifiedHtmlParagraph = StringSubstitutor.replace(htmlParagraph, values, "{", "}");
        addItemToEmailContents(modifiedHtmlParagraph);
        return this;
    }

    public ChplHtmlEmailBuilder addTable(List<String> tableHeadings, List<List<String>> tableData) {
        if (CollectionUtils.isEmpty(tableHeadings) && CollectionUtils.isEmpty(tableData)) {
            return this;
        }

        Map<String, String> values = new HashMap<String, String>();
        if (!CollectionUtils.isEmpty(tableHeadings)) {
            StringBuffer tableHeadingHtml = new StringBuffer();
            tableHeadingHtml.append("<tr>");
            tableHeadings.stream().forEach(heading -> tableHeadingHtml.append("<th align=\"left\">" + heading + "</th>"));
            tableHeadingHtml.append("</tr>");
            values.put(TABLE_HEADER_TAG, "<h2>" + tableHeadingHtml.toString() + "</h2>");
        } else {
            values.put(TABLE_HEADER_TAG, "");
        }
        if (!CollectionUtils.isEmpty(tableData)) {
            StringBuffer tableDataHtml = new StringBuffer();
            tableData.stream().forEach(row -> tableDataHtml.append("<tr>" + createRowHtml(row) + "</tr>"));
            values.put(TABLE_DATA_TAG, tableDataHtml.toString());
        } else {
            values.put(TABLE_DATA_TAG, "");
        }

        String modifiedHtmlTable = StringSubstitutor.replace(htmlTable, values, "{", "}");
        addItemToEmailContents(modifiedHtmlTable);
        return this;
    }

    private String createRowHtml(List<String> row) {
        StringBuffer rowHtml = new StringBuffer();
        row.stream()
            .forEach(cell -> rowHtml.append("<td>" + row + "</td>"));
        return rowHtml.toString();
    }

    public ChplHtmlEmailBuilder addButtonBar(Map<String, String> buttonLabelToHrefMap) {
        if (MapUtils.isEmpty(buttonLabelToHrefMap)) {
            return this;
        }

        StringBuilder buttonsHtml = new StringBuilder();
        buttonLabelToHrefMap.keySet().stream()
            .forEach(buttonLabel -> buttonsHtml.append(createButton(buttonLabel, buttonLabelToHrefMap.get(buttonLabel))));
        Map<String, String> values = new HashMap<String, String>();
        values.put(BUTTON_BAR_TAG, buttonsHtml.toString());

        String modifiedHtmlButtonBar = StringSubstitutor.replace(htmlButtonBar, values, "{", "}");
        addItemToEmailContents(modifiedHtmlButtonBar);
        return this;
    }

    private String createButton(String label, String href) {
        return "<td align=\"center\" "
                + "valign=\"top\" "
                + "style=\"padding: 10px;\"> "
                + "<a href=\"" + href + "\" "
                    + "style=\"background-color: #fff; "
                        + "border-radius: 5px; "
                        + "border: 1px solid #156dac; "
                        + "color: #156dac; "
                        + "display: inline-block; "
                        + "font-family: 'Play', sans-serif; "
                        + "font-size: 16px; "
                        + "line-height: 32px; "
                        + "text-align: center; "
                        + "text-decoration: none; "
                        + "width: 90%; "
                        + "-webkit-text-size-adjust: none; "
                        + "padding: 8px; "
                        + "margin-top: 10px; "
                        + "margin-bottom: 10px;\"> "
                + label + "</a>"
                + "</td>";
    }

    public ChplHtmlEmailBuilder addFooter() {
        addItemToEmailContents(htmlFooter);
        return this;
    }

    public String build() {
        Map<String, String> values = new HashMap<String, String>();
        values.put(EMAIL_CONTENT_TAG, "");
        return StringSubstitutor.replace(emailContents, values, "{", "}");
    }

    private void addItemToEmailContents(String htmlToAdd) {
        Map<String, String> values = new HashMap<String, String>();
        values.put(EMAIL_CONTENT_TAG, htmlToAdd + "\n" + EMAIL_CONTENT_TAG);
        StringSubstitutor.replace(emailContents, values, "{", "}");
    }
}