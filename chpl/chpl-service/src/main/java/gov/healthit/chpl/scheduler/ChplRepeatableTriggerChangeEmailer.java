package gov.healthit.chpl.scheduler;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplRepeatableTrigger;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.email.HtmlEmailTemplate;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ChplRepeatableTriggerChangeEmailer {

    private Environment environment;
    private CertificationBodyDAO certificationBodyDao;
    private String emailSubject;
    private String emailBody;
    private String emailStyles;

    @Autowired
    public ChplRepeatableTriggerChangeEmailer(Environment environment,
            CertificationBodyDAO certificationBodyDao, @Value("${job.change.subject}") String emailSubject,
            @Value("${job.change.body}") String emailBody, @Value("${email_styles}") String emailStyles) {

        this.environment = environment;
        this.certificationBodyDao = certificationBodyDao;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
        this.emailStyles = emailStyles;
    }

    public void sendEmail(ChplRepeatableTrigger trigger, ChplJob job, String action) throws MessagingException {
        EmailBuilder email = new EmailBuilder(environment);
        email.recipient(trigger.getEmail())
                .subject(emailSubject)
                .htmlMessage(getEmailText(trigger, job, action))
                .sendEmail();
    }

    private String getEmailText(ChplRepeatableTrigger trigger, ChplJob job, String action) {
        HtmlEmailTemplate email = new HtmlEmailTemplate();
        email.setStyles(emailStyles);
        email.setBody(getBody(trigger, job, action));
        return email.build();
    }

    private String getBody(ChplRepeatableTrigger trigger, ChplJob job, String action) {
        return String.format(emailBody, trigger.getJob().getName(), action, getTable(trigger, job));

    }

    private String getTable(ChplRepeatableTrigger trigger, ChplJob job) {
        StringBuilder table = new StringBuilder();
        table.append("<table class='blueTable'>\n");
        table.append("    <thead>\n");
        table.append("        <tr>\n");
        table.append("            <th colspan='2'>\n");
        table.append("                Job Values\n");
        table.append("            </th>\n");
        table.append("        </tr>\n");
        table.append("    </thead>\n");
        table.append("    <tbody>\n");
        table.append("        <tr class='odd'>\n");
        table.append("            <td>\n");
        table.append("                Email Address\n");
        table.append("            </td>\n");
        table.append("            <td>\n");
        table.append("                " + trigger.getEmail() + "\n");
        table.append("            </td>\n");
        table.append("        </tr>\n");
        table.append("        <tr class='even'>\n");
        table.append("            <td>\n");
        table.append("                Job Name\n");
        table.append("            </td>\n");
        table.append("            <td>\n");
        table.append("                " + job.getName() + "\n");
        table.append("            </td>\n");
        table.append("        </tr>\n");
        table.append("        <tr class='odd'>\n");
        table.append("            <td>\n");
        table.append("                Job Description\n");
        table.append("            </td>\n");
        table.append("            <td>\n");
        table.append("                " + job.getDescription() + "\n");
        table.append("            </td>\n");
        table.append("        </tr>\n");
        table.append("        <tr class='even'>\n");
        table.append("            <td>\n");
        table.append("                Schedule\n");
        table.append("            </td>\n");
        table.append("            <td>\n");
        table.append("                " + trigger.getCronSchedule() + "\n");
        table.append("            </td>\n");
        table.append("        </tr>\n");
        if (!StringUtils.isEmpty(trigger.getAcb())) {
            table.append("        <tr class='odd'>\n");
            table.append("            <td>\n");
            table.append("                Selected ONC-ACBs\n");
            table.append("            </td>\n");
            table.append("            <td>\n");
            table.append("                " + getAcbNamesAsCommaSeparatedList(trigger.getAcb()) + "\n");
            table.append("            </td>\n");
            table.append("        </tr>\n");
        }
        table.append("    </tbody>\n");
        table.append("</table>\n");
        return table.toString();
    }

    private String getAcbNamesAsCommaSeparatedList(String acbsCommaDelimited) {
        return Arrays.asList(
                acbsCommaDelimited.split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acbId -> {
                    try {
                        return certificationBodyDao.getById(Long.parseLong(acbId)).getName();
                    } catch (NumberFormatException | EntityRetrievalException e) {
                        LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                        return "";
                    }
                })
                .collect(Collectors.joining(", "));
    }
}
