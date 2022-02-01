package gov.healthit.chpl.email;

import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
    private MimeMessage message;
    private List<String> recipients;

    //optional parameters set to default
    private String subject = "";
    private String htmlBody = "";
    private String htmlFooter = "";
    private List<File> fileAttachments = null;
    private Environment env = null;
    //private SchedulerManager schedulerManager;
    private Scheduler scheduler;

    public EmailBuilder(Environment env, Scheduler scheduler) {
        this.env = env;
        //this.schedulerManager = scheduleManager;
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
        EmailOverrider overrider = new EmailOverrider(env);
        //Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));
        Session session = null;
        message = new MimeMessage(session);

        message.addRecipients(RecipientType.TO, overrider.getRecipients(recipients));
        message.setFrom(new InternetAddress(getProperties().getProperty("smtpFrom")));
        message.setSubject(this.subject);
        message.setSentDate(new Date());

        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(overrider.getBody(htmlBody + htmlFooter, recipients));

        if (fileAttachments != null) {
            // Add file attachments to email
            for (File file : fileAttachments) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());
                multipart.addBodyPart(messageBodyPart);
            }
        }
        message.setContent(multipart, "text/html; charset=UTF-8");

        return this;
    }

    public void sendEmail() throws EmailNotSentException {
       try {
           build();

           ChplOneTimeTrigger sendEmailTrigger = new ChplOneTimeTrigger();
           ChplJob sendEmailJob = new ChplJob();
           sendEmailJob.setName(SendEmailJob.JOB_NAME);
           //TODO : s/b a background job
           sendEmailJob.setGroup(SchedulerManager.SYSTEM_JOBS_KEY);
           JobDataMap jobDataMap = new JobDataMap();
           jobDataMap.put(SendEmailJob.MIME_MESSAGE_KEY, message);
           sendEmailJob.setJobDataMap(jobDataMap);
           sendEmailTrigger.setJob(sendEmailJob);
           sendEmailTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
           try {
               createOneTimeTrigger(sendEmailTrigger);
           } catch (SchedulerException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           } catch (ValidationException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
       } catch (Exception ex) {
           String failureMessage = "Email could not be sent to " + recipients.stream().collect(Collectors.joining(",")) + ".";
           //exception logged here so we can create an alert in DataDog
           LOGGER.fatal(failureMessage, ex);
           throw new EmailNotSentException(failureMessage);
       }
    }

//    private Authenticator getAuthenticator(Properties properties) {
//        return new Authenticator() {
//            @Override
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(properties.getProperty("smtpUsername"),
//                        properties.getProperty("smtpPassword"));
//            }
//        };
//    }

    private Properties getProperties() {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", env.getProperty("smtpHost"));
        properties.put("mail.smtp.port", env.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("smtpUsername", env.getProperty("smtpUsername"));
        properties.put("smtpPassword", env.getProperty("smtpPassword"));
        properties.put("smtpFrom", env.getProperty("smtpFrom"));

        return properties;
    }

    public ChplOneTimeTrigger createOneTimeTrigger(ChplOneTimeTrigger chplTrigger)
            throws SchedulerException, ValidationException {
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
