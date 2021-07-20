package gov.healthit.chpl.scheduler.job.deprecatedApiUsage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.HtmlEmailTemplate;

public class DeprecatedApiUsageEmailJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("deprecatedApiUsageEmailJobLogger");

    @Autowired
    private DeprecatedApiUsageDao deprecatedApiUsageDao;

    @Autowired
    private Environment env;

    @Value("{deprecatedApiUsage.email.subject}")
    private String deprecatedApiUsageEmailSubject;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Deprecated Api Usage Email job. *********");

        try {
            List<DeprecatedApiUsage> allDeprecatedApiUsage = deprecatedApiUsageDao.getAllDeprecatedApiUsage();
            Map<ApiKey, List<DeprecatedApiUsage>> deprecatedApiUsageByApiKey
                = allDeprecatedApiUsage.stream().collect(Collectors.groupingBy(DeprecatedApiUsage::getApiKey));

            deprecatedApiUsageByApiKey.keySet()
                .forEach(key -> sendEmailAndDeleteUsageRecords(key, deprecatedApiUsageByApiKey.get(key)));
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the Deprecated Api Usage Email job. *********");
    }

    private void sendEmailAndDeleteUsageRecords(ApiKey apiKey, List<DeprecatedApiUsage> deprecatedApiUsage) {
        LOGGER.info("API Key for " + apiKey.getEmail() + " has used " + deprecatedApiUsage.size() + " deprecated APIs.");
        EmailBuilder emailBuilder = new EmailBuilder(env);
        try {
            emailBuilder.recipient(apiKey.getEmail())
                .subject(deprecatedApiUsageEmailSubject)
                .htmlMessage("")
                .publicHtmlFooter()
                .sendEmail();
            LOGGER.info("Sent email to " + apiKey.getEmail() + ".");
            deprecatedApiUsage.stream().forEach(item -> deleteDeprecatedApiUsage(item));
        } catch (Exception ex) {
            LOGGER.error("Unable to send email to " + apiKey.getEmail() + ". "
                    + "User may not have been notified and database records will not be deleted.", ex);
        }
    }

    private void deleteDeprecatedApiUsage(DeprecatedApiUsage deprecatedApiUsage) {
        try {
            deprecatedApiUsageDao.delete(deprecatedApiUsage.getId());
            LOGGER.info("Deleted deprecated API usage with ID " + deprecatedApiUsage.getId());
        } catch (Exception ex) {
            LOGGER.error("Error deleting deprecated API usage with ID " + deprecatedApiUsage.getId(), ex);
        }
    }

    private class RwtEmail {
        private Environment env;

        RwtEmail(Environment env) {
            this.env = env;
        }

        public String getEmail(List<RealWorldTestingUpload> rwts) {
            HtmlEmailTemplate email = new HtmlEmailTemplate();
            email.setStyles(getStyles());
            email.setBody(getBody(rwts));
            return email.build();
        }

        private String getBody(List<RealWorldTestingUpload> rwts) {
            StringBuilder table = new StringBuilder();

            table.append("<table class='blueTable'>\n");
            table.append("    <thead>\n");
            table.append("        <tr>\n");
            table.append("            <th>\n");
            table.append("                CHPL Product Number\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Type\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Last Checked Date\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                URL\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Result or Errors\n");
            table.append("            </th>\n");
            table.append("        </tr>\n");
            table.append("    </thead>\n");
            table.append("    <tbody>\n");
            int i = 1;
            for (RealWorldTestingUpload rwt : rwts) {
                String trClass = i % 2 == 0 ? "even" : "odd";

                table.append("        <tr class=\"" + trClass + "\">\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getChplProductNumber());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getType());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getLastChecked());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getUrl());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(getErrorsAsString(rwt));
                table.append("            </td>\n");
                table.append("        </tr>\n");
                ++i;
            }
            table.append("    </tbody>\n");
            table.append("</table>\n");

            return table.toString();
        }

        private String getErrorsAsString(RealWorldTestingUpload rwt) {
            if (rwt.getValidationErrors().size() > 0) {
                return rwt.getValidationErrors().stream().map(err -> !err.startsWith("WARNING") ? "ERROR: " + err : err)
                        .collect(Collectors.joining("<br/>"));
            } else {
                return "SUCCESS";
            }
        }

        private String getStyles() {
            return env.getProperty("email_styles");
        }
    }
}
