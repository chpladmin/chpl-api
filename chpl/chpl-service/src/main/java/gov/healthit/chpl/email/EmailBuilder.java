package gov.healthit.chpl.email;

import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.SendEmailJob;
import lombok.extern.log4j.Log4j2;

/**
 * This class is used to send an email.  Properties are set using following a builder pattern.
 * Sample usage:
 *      EmailBuilder emailBuilder = new EmailBuilder(env);
 *      emailBuilder.recipients(recipients)
 *                  .subject(subject)
 *                  .htmlMessage(htmlMessage)
 *                  .fileAttachments(files)
 *                  .sendEmail();
 * @author TYoung
 *
 */
@Log4j2
public class EmailBuilder {
    private ChplEmailMessage message;

    private List<String> recipients;
    private String subject = "";
    private String htmlBody = "";
    private String htmlFooter = "";
    private List<File> fileAttachments = null;

    private Environment env = null;
    private Scheduler scheduler;

    public EmailBuilder(Environment env, Scheduler scheduler) {
        this.env = env;
        this.scheduler = scheduler;
    }

    public EmailBuilder recipients(List<String> addresses) {
        this.recipients = addresses;
        return this;
    }

    public EmailBuilder recipients(String[] addresses) {
        this.recipients = Arrays.asList(addresses);
        return this;
    }

    public EmailBuilder recipient(String address) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<String>();
        }
        this.recipients.clear();
        this.recipients.add(address);
        return this;
    }

    public EmailBuilder subject(String val) {
        subject = val;
        //Add the environment to the subject
        String suffix = "";
        if (!StringUtils.isEmpty(env.getProperty("emailBuilder.config.emailSubjectSuffix"))) {
            suffix = env.getProperty("emailBuilder.config.emailSubjectSuffix");
            subject = subject + " " + suffix;
        }
        return this;
    }

    public EmailBuilder htmlMessage(String val) {
        htmlBody = val;
        return this;
    }

    public EmailBuilder acbAtlHtmlFooter() {
        htmlFooter = String.format("<p>"
                + "If there are any questions about this process, please visit the "
                + "<a href=\"%s\">"
                + "ONC-ACB & ONC-ATL Portal</a> to submit a ticket."
                + "</p>"
                + "<p>Thank you!</p>"
                + "ONC CHPL Team",
                env.getProperty("footer.acbatlUrl"));
        return this;
    }

    public EmailBuilder publicHtmlFooter() {
        htmlFooter = String.format("<p>"
                + "If there are any questions about this process, please visit the "
                + "<a href=\"%s\">"
                + "Health IT Feedback and Inquiry Portal</a> to submit a ticket."
                + "</p>"
                + "<p>Thank you!</p>"
                + "ONC CHPL Team",
                env.getProperty("footer.publicUrl"));
        return this;
    }

    public EmailBuilder fileAttachments(List<File> val) {
        fileAttachments = val;
        return this;
    }

    //where it all comes together
    //this method is private and is called from sendEmail()
    private EmailBuilder build() throws AddressException, MessagingException {
        message = new ChplEmailMessage();
        message.setFileAttachments(fileAttachments);
        message.setBody(htmlBody + htmlFooter);
        message.setSubject(subject);
        message.setRecipients(recipients);
        return this;
    }

    public void sendEmail() throws EmailNotSentException {
       try {
           build();
           scheduleOneTimeTrigger(getOneTimeTrigger());
       } catch (Exception ex) {
           String failureMessage = "Email could not be sent to " + recipients.stream().collect(Collectors.joining(",")) + ".";
           LOGGER.fatal(failureMessage, ex);
           throw new EmailNotSentException(failureMessage);
       }
    }

    public ChplOneTimeTrigger getOneTimeTrigger() {
        ChplOneTimeTrigger sendEmailTrigger = new ChplOneTimeTrigger();
        ChplJob sendEmailJob = new ChplJob();
        sendEmailJob.setName(SendEmailJob.JOB_NAME);
        sendEmailJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SendEmailJob.MESSAGE_KEY, message);
        sendEmailJob.setJobDataMap(jobDataMap);
        sendEmailTrigger.setJob(sendEmailJob);
        sendEmailTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        return sendEmailTrigger;
    }

    public ChplOneTimeTrigger scheduleOneTimeTrigger(ChplOneTimeTrigger chplTrigger) throws SchedulerException, ValidationException {
        SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                .withIdentity(createTriggerName(chplTrigger), createTriggerGroup(chplTrigger.getJob()))
                .startAt(new Date(chplTrigger.getRunDateMillis()))
                .forJob(chplTrigger.getJob().getName(), chplTrigger.getJob().getGroup())
                .usingJobData(chplTrigger.getJob().getJobDataMap()).build();

        scheduler.scheduleJob(trigger);
        return chplTrigger;
    }

    private String createTriggerName(ChplOneTimeTrigger trigger) {
        return UUID.randomUUID().toString();
    }

    private String createTriggerGroup(ChplJob job) {
        return createTriggerGroup(job.getName());
    }

    private String createTriggerGroup(String triggerName) {
        String group = triggerName.replaceAll(" ", "");
        group += "Trigger";
        return group;
    }
}
