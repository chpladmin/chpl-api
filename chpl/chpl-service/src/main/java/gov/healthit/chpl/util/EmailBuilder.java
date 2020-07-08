package gov.healthit.chpl.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

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
public class EmailBuilder {
    private static Logger LOGGER = LogManager.getLogger(EmailBuilder.class);
    private MimeMessage message;
    private List<String> recipients;

    //optional parameters set to default
    private String subject = "";
    private String htmlBody = "";
    private String htmlFooter = "";
    private List<File> fileAttachments = null;
    private Environment env = null;

    /**
     * @param env - Spring Environment
     */
    public EmailBuilder(Environment env) {
        this.env = env;
    }


    /**
     * Sets the list of recipients for the email.
     * @param addresses - List of Strings representing email addresses
     * @return EmailBuilder (this)
     */
    public EmailBuilder recipients(List<String> addresses) {
        this.recipients = addresses;
        return this;
    }

    /**
     * Set a single recipient for the email.
     * @param addresses
     * @return
     */
    public EmailBuilder recipient(String address) {
        if (this.recipients == null) {
            this.recipients = new ArrayList<String>();
        }
        this.recipients.clear();
        this.recipients.add(address);
        return this;
    }

    /**
     * Sets the subject of the email.
     * @param val - the subject
     * @return EmailBuilder (this)
     */
    public EmailBuilder subject(String val) {
        subject = val;
        //Add the environment to the subject
        String suffix = "";
        if (!StringUtils.isEmpty(env.getProperty("emailBuilder.config.emailSubjectSuffix"))) {
            suffix = env.getProperty("emailBuilder.config.emailSubjectSuffix");
            subject = subject + " " +suffix;
        }
        return this;
    }

    /**
     * Sets the message of the email.
     * @param val - message in HTML form
     * @return EmailBuilder (this)
     */
    public EmailBuilder htmlMessage(String val) {
        htmlBody = val;
        return this;
    }

    public EmailBuilder htmlFooter() {
        htmlFooter = String.format("<p>"
                + "If there are any questions about this process, please visit the "
                + "<a href=\"%s\">"
                + "ONC-ACB & ONC-ATL Portal</a> to submit a ticket."
                + "</p>"
                + "<p>Thank you!</p>"
                + "ONC CHPL Team",
                env.getProperty("splitDeveloper.help.url"));
        return this;
    }

    /**
     * Sets the files to send as attachments with the email.
     * @param val - List of File objects
     * @return EmailBuilder (this)
     */
    public EmailBuilder fileAttachments(List<File> val) {
        fileAttachments = val;
        return this;
    }

    //where it all comes together
    //this method is private and is called from sendEmail()
    private EmailBuilder build() throws AddressException, MessagingException {
        EmailOverrider overrider = new EmailOverrider(env);
        Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));
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

    /**
     * Send the email that has been built.
     * @throws MessagingException - general exception that can occur for several reasons, see the message for
     * details
     */
    public void sendEmail() throws MessagingException {
       build();
       Transport.send(message);
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

        LOGGER.debug("Mail Host: " + env.getProperty("mail.smtp.host"));
        LOGGER.debug("Mail Port: " + env.getProperty("mail.smtp.port"));
        LOGGER.debug("Mail Username :" + env.getProperty("smtpUsername"));

        return properties;
    }
}
