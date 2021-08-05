package gov.healthit.chpl.email;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class HtmlEmailTemplate {

    private String styles;
    private String body;
    private String title;

    public String build() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        if (!StringUtils.isEmpty(styles)) {
            html.append("<style type=\"text/css\">");
            html.append(styles);
            html.append("</style>");
        }
        html.append("</head>");
        if (!StringUtils.isEmpty(body)) {
            html.append("<body>");
            html.append(body);
            html.append("</body>");
        }
        html.append("</html>");

        return html.toString();
    }
}