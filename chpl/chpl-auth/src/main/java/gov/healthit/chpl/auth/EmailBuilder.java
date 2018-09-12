package gov.healthit.chpl.auth;

import java.io.File;
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

public class EmailBuilder {
    private static final Logger LOGGER = LogManager.getLogger(EmailBuilder.class);
    private MimeMessage message;
    private List<String> recipients;
    
    //optional parameters set to default        
    private String subject = "";
    private String htmlBody = ""; 
    private List<File> fileAttachments = null;
    private Environment env = null;
    
    public EmailBuilder(Environment env) {
        this.env = env;
    }
    
    public EmailBuilder recipients(List<String> addresses) {
        this.recipients = addresses;
        return this;
    }
    
    public EmailBuilder subject(String val) {
        subject = val;
        return this;
    }
    
    public EmailBuilder htmlMessage(String val) {
        htmlBody = val;
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
        Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));
        message = new MimeMessage(session);
        
        message.addRecipients(RecipientType.TO, overrider.getRecipients(recipients));
        message.setFrom(new InternetAddress(getProperties().getProperty("smtpFrom")));
        message.setSubject(this.subject);
        message.setSentDate(new Date());
        
        Multipart multipart = new MimeMultipart();
        
        multipart.addBodyPart(overrider.getBody(htmlBody, recipients));
        
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
        message.setContent(multipart, "text/html");

        return this;
    }
    
    //send the email message
    public void sendEmail() throws AddressException, MessagingException {
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

        LOGGER.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
        LOGGER.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
        LOGGER.debug("Mail Username :" + env.getProperty("smtpUsername"));
        
        return properties;
    }
} 