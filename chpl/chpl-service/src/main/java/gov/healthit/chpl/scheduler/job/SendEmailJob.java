package gov.healthit.chpl.scheduler.job;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SendEmailJob implements Job {
    public static final String JOB_NAME = "sendEmailJob";
    public static final String MIME_MESSAGE_KEY = "mimeMessageKey";

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the API Key Del job. *********");

        MimeMessage messageWithoutSession = getMimeMessageFromMap(context);
        MimeMessage message = null;
        try (InputStream stream = messageWithoutSession.getInputStream()){
            Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));
            message = new MimeMessage(session, stream);

            Transport.send(message);
            LOGGER.info("Email successfully sent to: "
                    + getRecipientAddresses(message).stream().
                            map(addr -> addr.toString())
                            .collect(Collectors.joining(", ")));
        } catch (Exception ex) {
            String failureMessage;
            try {
                failureMessage = "Email could not be sent to "
                        + getRecipientAddresses(message).stream()
                                .map(addr -> addr.toString())
                                .collect(Collectors.joining(", "));
                //exception logged here so we can create an alert in DataDog
                //LOGGER.fatal(failureMessage, ex);
                //Calendar cal = Calendar.getInstance();
                //cal.add(Calendar.MINUTE, 5);

                //SimpleTriggerImpl retryTrigger = new SimpleTriggerImpl(Guid.NewGuid().ToString());
                //retryTrigger.setDescription("Retry" + context.getJobDetail().getKey().getName());
                //retryTrigger.setRepeatCount(0);
                //retryTrigger.setJobKey(context.getJobDetail().getKey());   // connect trigger with current job
                //retryTrigger.setStartTime(cal.getTime());
                //context.getScheduler().scheduleJob(retryTrigger);   // schedule the trigger

                //JobExecutionException ee = new JobExecutionException();
                //ee.setRefireImmediately(refire);

                //context.
                //context.getScheduler().rescheduleJob(triggerKey, newTrigger)



            } catch (MessagingException e) {
                LOGGER.error("Return could retreive list of recipients for this email.  We are not going to retruy sending this email.");
            }
        }





        LOGGER.info("********* Completed the API Key Deletion job. *********");
    }

    private MimeMessage getMimeMessageFromMap(JobExecutionContext context) {
        return (MimeMessage) context.getMergedJobDataMap().get("mimeMessage");
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
        properties.put("mail.smtp.host", env.getProperty("smtpHost"));
        properties.put("mail.smtp.port", env.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("smtpUsername", env.getProperty("smtpUsername"));
        properties.put("smtpPassword", env.getProperty("smtpPassword"));
        properties.put("smtpFrom", env.getProperty("smtpFrom"));

        return properties;
    }

    private List<Address> getRecipientAddresses(MimeMessage message) throws MessagingException {
        return Arrays.asList(message.getRecipients(Message.RecipientType.TO)).stream()
                    .collect(Collectors.toList());
    }

//    private Object createOneTimeTriggerToResendEmail() {
//        ChplOneTimeTrigger sendEmailTrigger = new ChplOneTimeTrigger();
//        ChplJob sendEmailJob = new ChplJob();
//        sendEmailJob.setName(JOB_NAME);
//        sendEmailJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
//        JobDataMap jobDataMap = new JobDataMap();
//
//        mergeDeveloperJob.setJobDataMap(jobDataMap);
//        mergeDeveloperTrigger.setJob(mergeDeveloperJob);
//        mergeDeveloperTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
//        mergeDeveloperTrigger = schedulerManager.createBackgroundJobTrigger(mergeDeveloperTrigger);
//        return mergeDeveloperTrigger;
//    }
}
