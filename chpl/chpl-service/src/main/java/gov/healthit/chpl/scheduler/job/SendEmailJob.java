package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailMessage;
import gov.healthit.chpl.email.EmailOverrider;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "sendEmailJobLogger")
@DisallowConcurrentExecution
public class SendEmailJob implements Job {
    public static final String JOB_NAME = "sendEmailJob";
    public static final String MESSAGE_KEY = "messageKey";
    private static final Integer UNLIMITED_RETRY_ATTEMPTS = -1;
    private static final String EMAIL_FILES_DIRECTORY = "emailFiles";

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Send Email job. *********");

        ChplEmailMessage message = (ChplEmailMessage) context.getMergedJobDataMap().get(MESSAGE_KEY);
        message.setRetryAttempts(message.getRetryAttempts() + 1);
        try {
            MimeMessage mimeMessage = getMimeMessage(message);
            Transport.send(mimeMessage);
            LOGGER.info("Email successfully sent to: "
                    + message.getRecipients().stream().
                            map(addr -> addr.toString())
                            .collect(Collectors.joining(", ")));
            LOGGER.info("With subject: " + message.getSubject());
            deleteFiles(message);
        } catch (Exception ex) {
            String failureMessage = "Error sending email to "
                    + message.getRecipients().stream()
                            .map(addr -> addr.toString())
                            .collect(Collectors.joining(", "));
            LOGGER.info(failureMessage);
            LOGGER.info("With subject: " + message.getSubject());
            LOGGER.info("Number of attempts: " + message.getRetryAttempts());
            LOGGER.info("Max number of attempts: " + (getMaxRetryAttempts() == -1 ? "unlimited" : getMaxRetryAttempts().toString()));

            LOGGER.catching(ex);

            if (getMaxRetryAttempts().equals(UNLIMITED_RETRY_ATTEMPTS)
                    || message.getRetryAttempts() < getMaxRetryAttempts()) {
                rescheduleEmailToBeSent(context, message);
            } else {
                // This should trigger a Datadog alert
                String error = "Email could not be sent to "
                        + message.getRecipients().stream()
                                .map(addr -> addr.toString())
                                .collect(Collectors.joining(", "));
                LOGGER.error(error);
                deleteFiles(message);
            }
        }
        LOGGER.info("********* Completed the Send Email job. *********");
    }



    private void rescheduleEmailToBeSent(JobExecutionContext context, ChplEmailMessage message) {
        message.setFileAttachments(copyFilesOnFirstReschedule(message));

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SendEmailJob.MESSAGE_KEY, message);

        Date retryTime = getRetryTime();
        Trigger retryTrigger = TriggerBuilder.newTrigger()
                .withDescription("Retry Email Trigger")
                .forJob(context.getJobDetail().getKey())
                .usingJobData(jobDataMap)
                .startAt(retryTime)
                .build();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        LOGGER.info("Email rescheduled to be sent at: " + df.format(retryTime));
        try {
            context.getScheduler().scheduleJob(retryTrigger);
        } catch (SchedulerException e) {
            LOGGER.error("Could not reschedule trigger due to exception: ", e);
        }
    }

    private Authenticator getAuthenticator(Properties properties) {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("smtpUsername"),
                        properties.getProperty("smtpPassword"));
            }
        };
    }

    private Properties getProperties() {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.host", env.getProperty("smtpHost"));
        properties.put("mail.smtp.port", env.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("smtpUsername", env.getProperty("smtpUsername"));
        properties.put("smtpPassword", env.getProperty("smtpPassword"));
        properties.put("smtpFrom", env.getProperty("smtpFrom"));

        return properties;
    }

    private MimeMessage getMimeMessage(ChplEmailMessage message) throws MessagingException {

        EmailOverrider overrider = new EmailOverrider(env);
        Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.addRecipients(RecipientType.TO, overrider.getRecipients(message.getRecipients()));
        mimeMessage.setFrom(new InternetAddress(getProperties().getProperty("smtpFrom")));
        mimeMessage.setSubject(message.getSubject());
        mimeMessage.setSentDate(new Date());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(overrider.getBody(message.getBody(), message.getRecipients()));
        if (message.getFileAttachments() != null && message.getFileAttachments().size() > 0) {
            // Add file attachments to email
            for (File file : message.getFileAttachments()) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());
                multipart.addBodyPart(messageBodyPart);
            }
        }
        mimeMessage.setContent(multipart, "text/html; charset=UTF-8");
        return mimeMessage;
    }

    private Date getRetryTime() {
        Integer retryInterval = Integer.valueOf(env.getProperty("emailRetryIntervalInMinutes"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, retryInterval);
        return cal.getTime();
    }

    private Integer getMaxRetryAttempts() {
        return Integer.valueOf(env.getProperty("emailRetryAttempts"));
    }

    private List<File> copyFilesOnFirstReschedule(ChplEmailMessage message) {
        if (message.getRetryAttempts().equals(1) && message.getFileAttachments() != null) {
            if (message.getFileAttachments() == null || message.getFileAttachments().size() == 0) {
                LOGGER.info("No files to move.");
            }
            return  message.getFileAttachments().stream()
                    .map(file -> copyTempFileToPermanentLocation(file))
                    .collect(Collectors.toList());
        } else {
            return message.getFileAttachments();
        }

    }

    private File copyTempFileToPermanentLocation(File originalFile) {
        Path newPath = Paths.get(env.getProperty("downloadFolderPath") + File.separator + EMAIL_FILES_DIRECTORY + File.separator + originalFile.getName());
        Path origPath = originalFile.toPath();
        try {
            Files.createDirectories(newPath.getParent());
            Files.copy(origPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.info("Could not copy original file: " + origPath.toString());
            LOGGER.catching(e);
        }
        LOGGER.info("Copied " + origPath.toString() + " to " + newPath.toString());
        return newPath.toFile();
    }

    private void deleteFiles(ChplEmailMessage message) {
        if (message.getFileAttachments() != null) {
            message.getFileAttachments().stream()
                    .forEach(file -> {
                        try {
                            Files.deleteIfExists(file.toPath());
                            LOGGER.info("Deleting file: " + file.getPath());
                        } catch (IOException e) {
                            LOGGER.info("Could not delete file after sending: " + file.getPath());
                            LOGGER.catching(e);
                        }
                    });
        }
    }
}
