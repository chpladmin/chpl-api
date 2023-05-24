package gov.healthit.chpl.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChplHtmlEmailBuilder {
    private static final String EMAIL_CONTENT_TAG = "${email-content}";
    private static final String TITLE_TAG = "${title}";
    private static final String PARAGRAPH_HEADING_TAG = "${paragraph-heading}";
    private static final String PARAGRAPH_TEXT_TAG = "${paragraph-text}";
    private static final String TABLE_HEADER_TAG = "${table-header}";
    private static final String TABLE_DATA_TAG = "${table-data}";
    private static final String TABLE_CAPTION_TAG = "${table-caption}";
    private static final String BUTTON_BAR_TAG = "${buttons}";
    private static final String FEEDBACK_URL_TAG = "${feedback-url}";
    private static final String EMPTY_TABLE_DEFAULT_TEXT = "No Applicable Data";
    private static final String DEFAULT_PARAGRAPH_HEADING_LEVEL = "h2";

    private String htmlSkeleton;
    private String htmlHeading;
    private String htmlParagraph;
    private String htmlTable;
    private String htmlButtonBar;
    private String htmlFooter;
    private String publicFeedbackUrl;
    private String adminAcbAndAtlFeedbackUrl;
    private String emailContents;

    @Autowired
    public ChplHtmlEmailBuilder(@Value("classpath:email/chpl-email-skeleton.html") Resource htmlSkeletonResource,
            @Value("classpath:email/chpl-email-heading.html") Resource htmlHeadingResource,
            @Value("classpath:email/chpl-email-paragraph.html") Resource htmlParagraphResource,
            @Value("classpath:email/chpl-email-table.html") Resource htmlTableResource,
            @Value("classpath:email/chpl-email-button-bar.html") Resource htmlButtonBarResource,
            @Value("classpath:email/chpl-email-footer.html") Resource htmlFooterResource,
            @Value("${contact.acbatlUrl}") String adminAcbAndAtlFeedbackUrl,
            @Value("${contact.publicUrl}") String publicFeedbackUrl) throws IOException {
        htmlSkeleton = StreamUtils.copyToString(htmlSkeletonResource.getInputStream(), StandardCharsets.UTF_8);
        htmlHeading = StreamUtils.copyToString(htmlHeadingResource.getInputStream(), StandardCharsets.UTF_8);
        htmlParagraph = StreamUtils.copyToString(htmlParagraphResource.getInputStream(), StandardCharsets.UTF_8);
        htmlTable = StreamUtils.copyToString(htmlTableResource.getInputStream(), StandardCharsets.UTF_8);
        htmlButtonBar = StreamUtils.copyToString(htmlButtonBarResource.getInputStream(), StandardCharsets.UTF_8);
        htmlFooter = StreamUtils.copyToString(htmlFooterResource.getInputStream(), StandardCharsets.UTF_8);
        this.adminAcbAndAtlFeedbackUrl = adminAcbAndAtlFeedbackUrl;
        this.publicFeedbackUrl = publicFeedbackUrl;
        emailContents = new String(htmlSkeleton);
    }

    public ChplHtmlEmailBuilder initialize() {
        this.emailContents = new String(htmlSkeleton);
        return this;
    }

    public ChplHtmlEmailBuilder heading(String title) {
        String modifiedHtmlHeading = new String(htmlHeading);
        if (!StringUtils.isEmpty(title)) {
            modifiedHtmlHeading = modifiedHtmlHeading.replace(TITLE_TAG, "<h1>" + title + "</h1>");
        } else {
            modifiedHtmlHeading = modifiedHtmlHeading.replace(TITLE_TAG, "");
        }

        addItemToEmailContents(modifiedHtmlHeading);
        return this;
    }

    public ChplHtmlEmailBuilder paragraph(String heading, String text) {
        return paragraph(heading, text, "h2");
    }

    public ChplHtmlEmailBuilder paragraph(String heading, String text, String headingLevel) {
        if (StringUtils.isAllBlank(heading, text)) {
            return this;
        }

        String modifiedHtmlParagraph = getParagraphHtml(heading, text, headingLevel);
        addItemToEmailContents(modifiedHtmlParagraph);
        return this;
    }

    public ChplHtmlEmailBuilder table(List<String> tableHeadings, List<List<String>> tableData) {
        return table(tableHeadings, tableData, EMPTY_TABLE_DEFAULT_TEXT);
    }

    public ChplHtmlEmailBuilder table(List<String> tableHeadings, List<List<String>> tableData, String emptyDataText) {
        return table(tableHeadings, tableData, emptyDataText, null);
    }

    public ChplHtmlEmailBuilder table(List<String> tableHeadings, List<List<String>> tableData, String tableCaption,
            String emptyDataText) {
        if (CollectionUtils.isEmpty(tableHeadings) && CollectionUtils.isEmpty(tableData)) {
            return this;
        }

        String modifiedHtmlTable = getTableHtml(tableHeadings, tableData, emptyDataText, tableCaption);
        addItemToEmailContents(modifiedHtmlTable);
        return this;
    }

    private String tableRow(List<String> row) {
        StringBuffer rowHtml = new StringBuffer();
        row.stream()
            .forEach(cell -> rowHtml.append("<td>" + cell + "</td>"));
        return rowHtml.toString();
    }

    public ChplHtmlEmailBuilder buttonBar(Map<String, String> buttonLabelToHrefMap) {
        if (MapUtils.isEmpty(buttonLabelToHrefMap)) {
            return this;
        }

        StringBuilder buttonsHtml = new StringBuilder();
        buttonLabelToHrefMap.keySet().stream()
            .forEach(buttonLabel -> buttonsHtml.append(button(buttonLabel, buttonLabelToHrefMap.get(buttonLabel))));

        String modifiedHtmlButtonBar = htmlButtonBar.replace(BUTTON_BAR_TAG, buttonsHtml.toString());
        addItemToEmailContents(modifiedHtmlButtonBar);
        return this;
    }

    private String button(String label, String href) {
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

    public ChplHtmlEmailBuilder footer(boolean publicUrl) {
        String modifiedHtmlFooter = new String(htmlFooter);
        if (!publicUrl) {
            modifiedHtmlFooter = modifiedHtmlFooter.replace(FEEDBACK_URL_TAG, adminAcbAndAtlFeedbackUrl);
        } else {
            modifiedHtmlFooter = modifiedHtmlFooter.replace(FEEDBACK_URL_TAG, publicFeedbackUrl);
        }

        addItemToEmailContents(modifiedHtmlFooter);
        return this;
    }

    public ChplHtmlEmailBuilder customHtml(String htmlToAdd) {
        addItemToEmailContents(htmlToAdd);
        return this;
    }

    public void addItemToEmailContents(String htmlToAdd) {
        emailContents = emailContents.replace(EMAIL_CONTENT_TAG, htmlToAdd + EMAIL_CONTENT_TAG);
    }

    public String build() {
        return emailContents.replace(EMAIL_CONTENT_TAG, "");
    }

    public String getTableHtml(List<String> tableHeadings, List<List<String>> tableData, String emptyDataText) {
        return getTableHtml(tableHeadings, tableData, emptyDataText, null);
    }

    public String getTableHtml(List<String> tableHeadings, List<List<String>> tableData, String emptyDataText,
            String tableCaption) {
        String customHtmlTable = new String(htmlTable);
        if (!CollectionUtils.isEmpty(tableHeadings)) {
            StringBuffer tableHeadingHtml = new StringBuffer();
            tableHeadingHtml.append("<tr>");
            tableHeadings.stream().forEach(heading -> tableHeadingHtml.append("<th align=\"left\">" + heading + "</th>"));
            tableHeadingHtml.append("</tr>");
            customHtmlTable = customHtmlTable.replace(TABLE_HEADER_TAG,  tableHeadingHtml.toString());
        } else {
            customHtmlTable = customHtmlTable.replace(TABLE_HEADER_TAG, "");
        }
        if (!CollectionUtils.isEmpty(tableData)) {
            StringBuffer tableDataHtml = new StringBuffer();
            tableData.stream().forEach(row -> tableDataHtml.append("<tr>" + tableRow(row) + "</tr>"));
            customHtmlTable = customHtmlTable.replace(TABLE_DATA_TAG, tableDataHtml.toString());
        } else if (CollectionUtils.isEmpty(tableData) && !CollectionUtils.isEmpty(tableHeadings)
                && !StringUtils.isEmpty(emptyDataText)) {
            customHtmlTable = customHtmlTable.replace(TABLE_DATA_TAG,
                    "<tr><td colspan=\"" + tableHeadings.size() + "\">" + emptyDataText + "</td></tr>");
        } else {
            customHtmlTable = customHtmlTable.replace(TABLE_DATA_TAG, "");
        }

        if (!StringUtils.isEmpty(tableCaption)) {
            customHtmlTable = customHtmlTable.replace(TABLE_CAPTION_TAG,
                    "<tr><td colspan=\"" + tableHeadings.size() + "\">" + tableCaption + "</td></tr>");
        } else {
            customHtmlTable = customHtmlTable.replace(TABLE_CAPTION_TAG, "");
        }
        return customHtmlTable;
    }

    public String getParagraphHtml(String heading, String text, String headingLevel) {
        if (StringUtils.isEmpty(headingLevel)) {
            headingLevel = DEFAULT_PARAGRAPH_HEADING_LEVEL;
        }

        String customHtmlParagraph = new String(htmlParagraph);
        if (!StringUtils.isEmpty(heading)) {
            customHtmlParagraph = customHtmlParagraph.replace(PARAGRAPH_HEADING_TAG,
                    "<" + headingLevel + ">" + heading + "</" + headingLevel + ">");
        } else {
            customHtmlParagraph = customHtmlParagraph.replace(PARAGRAPH_HEADING_TAG, "");
        }
        if (!StringUtils.isEmpty(text)) {
            customHtmlParagraph = customHtmlParagraph.replace(PARAGRAPH_TEXT_TAG, "<p>" + text + "</p>");
        } else {
            customHtmlParagraph = customHtmlParagraph.replace(PARAGRAPH_TEXT_TAG, "");
        }
        return customHtmlParagraph;
    }
}