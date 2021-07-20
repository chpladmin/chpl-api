package gov.healthit.chpl.util;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class HtmlEmailTemplate {

    private String styles;
    private String body;
    private String title;
    private String subtitle;

    public String build() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"en\">");
        html.append("<head>");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />");
        html.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />");
        if (!StringUtils.isEmpty(styles)) {
            html.append("<style type=\"text/css\">");
            html.append(styles);
            html.append("</style>");
        } else {
            html.append(getDefaultEmailStyle());
        }
        html.append("</head>");
        html.append("<body style=\"margin: 0; padding: 0;\">");
        html.append("<center>\n"
                + "      <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n"
                + "        <tr>\n"
                + "          <td align=\"center\" valign=\"top\">\n"
                + "          <table\n"
                + "              width=\"800\"\n"
                + "              cellspacing=\"0\"\n"
                + "              cellpadding=\"0\"\n"
                + "              border=\"0\"\n"
                + "              align=\"center\"\n"
                + "              style=\"max-width: 800px; width: 100%;\"\n"
                + "              bgcolor=\"#FFFFFF\"\n"
                + "                        >\n"
                + "              <!-- CTA Text -->      \n"
                + "              <tr>\n"
                + "                <td align=\"center\" valign=\"top\">\n"
                + "                  <table\n"
                + "                    bgcolor=\"#156dac\"\n"
                + "                    background=\"data:image/png;base64,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\"\n"
                + "                    width=\"800\"\n"
                + "                    height=\"200px\"\n"
                + "                    cellspacing=\"0\"\n"
                + "                    cellpadding=\"0\"\n"
                + "                    border=\"0\"\n"
                + "                    align=\"center\"\n"
                + "                    style=\"max-width: 800px; width: 100%;\"\n"
                + "                  >"
                + "                   <tr>\n"
                + "                      <td\n"
                + "                        align=\"center\"\n"
                + "                        valign=\"top\"\n"
                + "                        style=\"\n"
                + "                          padding-top: 64px;\n"
                + "                          font-size: 1em;\n"
                + "                          color: white;\n"
                + "                          font-family: 'Open Sans', sans-serif;\n"
                + "                                                    font-weight:700;\n"
                + "                          padding-left: 16px;\n"
                + "                          padding-right: 16px;\n"
                + "                        \"\n"
                + "                      >\n"
                + "                                            <h1>" + getTitle() + "</h1>\n"
                + "                                            <p\n"
                + "                                            style=\"\n"
                + "                          margin-top: -10px;\n"
                + "                          font-size: 12px;\n"
                + "                          color: white;\n"
                + "                          font-family: 'Open Sans', sans-serif;\n"
                + "                                                    font-weight:400;\n"
                + "                          padding-bottom: 64px;\n"
                + "                        \"\n"
                + "                                            >" + getSubtitle() + "</p>\n"
                + "                      </td>\n"
                + "                    </tr>\n"
                + "                  </table>\n"
                + "                </td>\n"
                + "              </tr>"
                + "              <tr>\n"
                + "                <td align=\"center\" valign=\"top\">\n"
                + "                  <table\n"
                + "                    width=\"800\"\n"
                + "                    cellspacing=\"0\"\n"
                + "                    cellpadding=\"0\"\n"
                + "                    border=\"0\"\n"
                + "                    align=\"center\"\n"
                + "                    style=\"max-width: 800px; width: 100%;\"\n"
                + "                    bgcolor=\"#FFFFFF\"\n"
                + "                  >\n"
                + "                    <tr>\n"
                + "                      <td align=\"center\" valign=\"top\">\n"
                + "                        <table\n"
                + "                          \n"
                + "                          width=\"800\"\n"
                + "                          cellspacing=\"0\"\n"
                + "                          cellpadding=\"0\"\n"
                + "                          border=\"0\"\n"
                + "                          align=\"center\"\n"
                + "                          style=\"max-width: 800px; width: 100%;\n"
                + "                          background-color:#ffffff;\"\n"
                + "                        >\n"
                + "                          <tr>\n"
                + "                            <td\n"
                + "                              valign=\"top\"\n"
                + "                              style=\"\n"
                + "                                padding:16px;\n"
                + "                                font-size: 18px;\n"
                + "                                color: #000;\n"
                + "                                font-family: 'Lato', sans-serif;\n"
                + "                              \"\n"
                + "                            >");
        if (!StringUtils.isEmpty(body)) {
            html.append(body);
        }
        html.append("</tr>\n"
                + "</table>\n"
                + "</td>\n"
                + "</tr>\n"
                + "</table>\n"
                + "</td>\n"
                + "</tr>");
        html.append(getFooterBar());
        html.append("</table>\n"
                + " </center>\n"
                + "</body>\""
                + "</html>");

        return html.toString();
    }

    private String getDefaultEmailStyle() {
        return  "<style type=\"text/css\">"
                    + "table tr {\n"
                    + "  font-family:Arial, Helvetica, sans-serif;\n"
                    + "  border:1px solid #eeeeee;\n"
                    + "}\n"
                    + "table tr:hover {\n"
                    + "   background-color: #ddd;\n"
                    + "}\n"
                    + "table th {\n"
                    + "   color:#156dac;\n"
                    + "   border:1px solid #eeeeee;\n"
                    + "   font-family:Arial, Helvetica, sans-serif;\n"
                    + "}\n"
                + "</style>";
    }

    private String getFooterBar() {
        return "<!-- Footer Bar -->\n"
                + "              <tr>\n"
                + "                <td align=\"center\" valign=\"top\">\n"
                + "                  <table\n"
                + "                    width=\"800\"\n"
                + "                    cellspacing=\"0\"\n"
                + "                    cellpadding=\"0\"\n"
                + "                    border=\"0\"\n"
                + "                    align=\"center\"\n"
                + "                    style=\"max-width: 800px; width: 100%;\"\n"
                + "                    bgcolor=\"#FFFFFF\"\n"
                + "                  >\n"
                + "                  <td style=\"padding: 0;\">\n"
                + "                      <table width=\"100%\" align=\"center\" style=\"border-spacing: 0;\">\n"
                + "                        <tr>\n"
                + "                          <td\n"
                + "                            width=\"100%\"\n"
                + "                            height=\"64px\"\n"
                + "                            style=\"background-color: #dedede; color: #ffffff; padding: 0;\"\n"
                + "                          >\n"
                + "                            <table width=\"100%\" style=\"border-spacing: 0;\">\n"
                + "                              <tr>\n"
                + "                                <!-- inner empty spacing -->\n"
                + "                                <td width=\"26px\" class=\"spacing\" style=\"padding: 0;\"></td>\n"
                + "            \n"
                + "                                <!-- page nav -->\n"
                + "                                <td\n"
                + "                                  align=\"left\"\n"
                + "                                  style=\"\n"
                + "                                    font-size: 12px;\n"
                + "                                    line-height: 18px;\n"
                + "                                    font-family: Calibri, sans-serif;\n"
                + "                                    padding: 0;\n"
                + "                                    color: #000000;\n"
                + "                                  \"\n"
                + "                                >\n"
                + "                                  <a\n"
                + "                                    href=\"https://chpl.healthit.gov/\"\n"
                + "                                    target=\"_blank\"\n"
                + "                                    style=\"color: #156dac; text-decoration: underline;\"\n"
                + "                                    >Home</a\n"
                + "                                  >\n"
                + "                                  |\n"
                + "                                  <a style=\"color: #000000; text-decoration: none;\">\n"
                + "                                    © 2021 CHPL. All rights reserved\n"
                + "                                  </a>\n"
                + "                                </td>\n"
                + "            \n"
                + "                                <!-- inner empty spacing -->\n"
                + "                                <td width=\"16px\" class=\"spacing\" style=\"padding: 0;\"></td>\n"
                + "                              </tr>\n"
                + "                            </table>\n"
                + "                          </td>\n"
                + "                        </tr>\n"
                + "                      </table>\n"
                + "                    </td>\n"
                + "              </tr>\n";
    }
}