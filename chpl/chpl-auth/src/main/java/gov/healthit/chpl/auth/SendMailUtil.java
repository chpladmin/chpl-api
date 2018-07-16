package gov.healthit.chpl.auth;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Utility class for email sending.
 *
 * @author dlucas
 *
 */
@Service("SendMailUtil")
public class SendMailUtil {

    private static final Logger LOGGER = LogManager.getLogger(SendMailUtil.class);
    @Autowired
    private Environment env;

    /**
     * Create and send an email with the provided string[] toEmails, subject and
     * HTML message.
     *
     * @param toEmail addresses to send To
     * @param bccEmail addresses to send BCC
     * @param subject subject of email
     * @param htmlMessage  message
     * @throws AddressException if address fails
     * @throws MessagingException if messaging fails
     */
    public void sendEmail(final String[] toEmail, final String[] bccEmail, final String subject,
            final String htmlMessage) throws AddressException, MessagingException {
        // do not attempt to send email if we are in a dev environment
        String mailHost = env.getProperty("smtpHost");
        if (StringUtils.isEmpty(mailHost) || "development".equalsIgnoreCase(mailHost)
                || "dev".equalsIgnoreCase(mailHost)) {
            return;
        }

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", env.getProperty("smtpHost"));
        properties.put("mail.smtp.port", env.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("smtpUsername", env.getProperty("smtpUsername"));
        properties.put("smtpPassword", env.getProperty("smtpPassword"));
        properties.put("smtpFrom", env.getProperty("smtpFrom"));

        LOGGER.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
        LOGGER.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
        LOGGER.debug("Mail Username :" + env.getProperty("smtpUsername"));

        sendEmail(toEmail, bccEmail, subject, htmlMessage, properties);
    }

    /**
     * Create and send an email with the provided string[] toEmails, subject and
     * HTML message.
     *
     * @param toEmail addresses to send To
     * @param bccEmail addresses to send BCC
     * @param subject subject of email
     * @param htmlMessage message
     * @param properties properties object to pull authentication from
     * @throws AddressException if address fails
     * @throws MessagingException if messaging fails
     */
    public void sendEmail(final String[] toEmail, final String[] bccEmail, final String subject,
            final String htmlMessage, final Properties properties) throws AddressException, MessagingException {
        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("smtpUsername"),
                        properties.getProperty("smtpPassword"));
            }
        };

        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        try {
            InternetAddress fromEmail = new InternetAddress(properties.getProperty("smtpFrom"));
            msg.setFrom(fromEmail);
            LOGGER.debug("Sending email from " + properties.getProperty("smtpFrom"));
        } catch (MessagingException ex) {
            LOGGER.fatal("Invalid Email Address: " + properties.getProperty("smtpFrom"), ex);
            throw ex;
        }

        try {
            if (toEmail != null && toEmail.length > 0) {
                InternetAddress[] toAddresses = new InternetAddress[toEmail.length];
                for (int i = 0; i < toEmail.length; i++) {
                    InternetAddress toEmailaddress = new InternetAddress(toEmail[i]);
                    toAddresses[i] = toEmailaddress;
                }
                msg.setRecipients(Message.RecipientType.TO, toAddresses);
                LOGGER.debug("Sending email to " + Arrays.toString(toEmail));
            } else if (bccEmail != null && bccEmail.length > 0) {
                InternetAddress[] bccAddresses = new InternetAddress[bccEmail.length];
                for (int i = 0; i < bccEmail.length; i++) {
                    InternetAddress bccAddress = new InternetAddress(bccEmail[i]);
                    bccAddresses[i] = bccAddress;
                }
                msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
                LOGGER.debug("Sending email to " + Arrays.toString(bccEmail));
            }
        } catch (MessagingException ex) {
            LOGGER.fatal("Invalid Email Address: " + Arrays.toString(toEmail), ex);
            throw ex;
        }

        msg.setSubject(subject);
        msg.setSentDate(new Date());
        // set plain text message
        msg.setContent(htmlMessage, "text/html");

        // sends the e-mail
        Transport.send(msg);
    }

    /**
     * Send an email using the given properties and list of files to attach.
     *
     * @param toEmail address to send To
     * @param subject subject of email
     * @param htmlMessage message
     * @param files files to attach to the message
     * @param props properties object to pull authentication from
     * @throws AddressException if address fails
     * @throws MessagingException if messaging fails
     */
    public void sendEmail(final String toEmail, final String subject, final String htmlMessage,
            final List<File> files, final Properties props)
                    throws AddressException, MessagingException {
        // do not attempt to send email if we are in a dev environment
        String mailHost = props.getProperty("smtpHost");
        if (StringUtils.isEmpty(mailHost) || "development".equalsIgnoreCase(mailHost)
                || "dev".equalsIgnoreCase(mailHost)) {
            return;
        }

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", props.getProperty("smtpHost"));
        properties.put("mail.smtp.port", props.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        LOGGER.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
        LOGGER.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
        LOGGER.debug("Mail Username :" + props.getProperty("smtpUsername"));

        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("smtpUsername"), props.getProperty("smtpPassword"));
            }
        };

        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        try {
            InternetAddress fromEmail = new InternetAddress(props.getProperty("smtpFrom"));
            msg.setFrom(fromEmail);
            LOGGER.debug("Sending email from " + props.getProperty("smtpFrom"));
        } catch (MessagingException ex) {
            LOGGER.fatal("Invalid Email Address: " + props.getProperty("smtpFrom"), ex);
            throw ex;
        }

        try {
            InternetAddress toAddress = new InternetAddress(toEmail);
            msg.setRecipient(Message.RecipientType.TO, toAddress);
            LOGGER.debug("Sending email to " + toEmail);
        } catch (MessagingException ex) {
            LOGGER.fatal("Invalid Email Address: " + toEmail, ex);
            throw ex;
        }

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        BodyPart messageBodyPartWithMessage = new MimeBodyPart();
        messageBodyPartWithMessage.setContent(htmlMessage, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPartWithMessage);

        if (files != null) {
            // Add file attachments to email
            for (File file : files) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());

                multipart.addBodyPart(messageBodyPart);
            }
        }
        msg.setContent(multipart, "text/html");

        // sends the e-mail
        Boolean emailSentSuccessfully = false;
        try {
            Transport.send(msg);
            emailSentSuccessfully = true;
            LOGGER.info("email sent successfully");
        } catch (SendFailedException e) {
            LOGGER.info("SendFailedException. " + e.getMessage());
        } catch (AuthenticationFailedException e) {
            LOGGER.info("AuthenticationFailedException. " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.info("MessagingException. " + e.getMessage());
        } catch (Exception e) {
            LOGGER.info("Exception while sending email. " + e.getMessage());
        }

        if (!emailSentSuccessfully) {
            LOGGER.info("Email did not send successfully.");
        }
    }

    /**
     * Send an email using the given properties and list of files to attach.
     * Uses the toEmail if non-null or the bccEmail if non-null. Also uses the
     * subject and htmlMessage
     *
     * @param toEmail addresses to send To
     * @param bccEmail addresses to send BCC
     * @param subject subject of email
     * @param htmlMessage message
     * @param files files to attach to the message
     * @param props properties object to pull authentication from
     * @throws AddressException if address fails
     * @throws MessagingException if messaging fails
     */
    public void sendEmail(final String[] toEmail, final String[] bccEmail, final String subject,
            final String htmlMessage, final List<File> files, final Properties props)
                    throws AddressException, MessagingException {
        // do not attempt to send email if we are in a dev environment
        String mailHost = props.getProperty("smtpHost");
        if (StringUtils.isEmpty(mailHost) || "development".equalsIgnoreCase(mailHost)
                || "dev".equalsIgnoreCase(mailHost)) {
            return;
        }

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", props.getProperty("smtpHost"));
        properties.put("mail.smtp.port", props.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        LOGGER.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
        LOGGER.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
        LOGGER.debug("Mail Username :" + props.getProperty("smtpUsername"));

        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("smtpUsername"), props.getProperty("smtpPassword"));
            }
        };

        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        try {
            InternetAddress fromEmail = new InternetAddress(props.getProperty("smtpFrom"));
            msg.setFrom(fromEmail);
            LOGGER.debug("Sending email from " + props.getProperty("smtpFrom"));
        } catch (MessagingException ex) {
            LOGGER.fatal("Invalid Email Address: " + props.getProperty("smtpFrom"), ex);
            throw ex;
        }

        if (toEmail != null) {
            try {
                InternetAddress[] toAddresses = new InternetAddress[toEmail.length];
                for (int i = 0; i < toEmail.length; i++) {
                    InternetAddress toEmailaddress = new InternetAddress(toEmail[i]);
                    toAddresses[i] = toEmailaddress;
                }
                msg.setRecipients(Message.RecipientType.TO, toAddresses);
                LOGGER.debug("Sending email to " + Arrays.toString(toEmail));
            } catch (MessagingException ex) {
                LOGGER.fatal("Invalid Email Address: " + Arrays.toString(toEmail), ex);
                throw ex;
            }
        }

        if (bccEmail != null) {
            try {
                InternetAddress[] bccAddresses = new InternetAddress[bccEmail.length];
                for (int i = 0; i < bccEmail.length; i++) {
                    InternetAddress bccEmailaddress = new InternetAddress(bccEmail[i]);
                    bccAddresses[i] = bccEmailaddress;
                }
                msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
                LOGGER.debug("Sending email bcc to " + Arrays.toString(bccEmail));
            } catch (MessagingException ex) {
                LOGGER.fatal("Invalid Email Address: " + Arrays.toString(bccEmail), ex);
                throw ex;
            }
        }

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        BodyPart messageBodyPartWithMessage = new MimeBodyPart();
        messageBodyPartWithMessage.setContent(htmlMessage, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPartWithMessage);

        if (files != null) {
            // Add file attachments to email
            for (File file : files) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());

                multipart.addBodyPart(messageBodyPart);
            }
        }
        msg.setContent(multipart, "text/html");

        // sends the e-mail
        Boolean emailSentSuccessfully = false;
        try {
            Transport.send(msg);
            emailSentSuccessfully = true;
            LOGGER.info("email sent successfully");
        } catch (SendFailedException e) {
            LOGGER.info("SendFailedException. " + e.getMessage());
        } catch (AuthenticationFailedException e) {
            LOGGER.info("AuthenticationFailedException. " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.info("MessagingException. " + e.getMessage());
        } catch (Exception e) {
            LOGGER.info("Exception while sending email. " + e.getMessage());
        }

        if (!emailSentSuccessfully) {
            LOGGER.info("Email did not send successfully.");
        }
    }
}
