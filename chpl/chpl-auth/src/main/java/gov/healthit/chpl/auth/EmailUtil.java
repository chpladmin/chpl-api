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

public class EmailUtil {
    private static final Logger LOGGER = LogManager.getLogger(EmailUtil.class);
    private MimeMessage message;
    private List<String> toAddresses;
    
    //optional parameters set to default        
    private String subject = "";
    private String htmlBody = ""; 
    private Boolean useSignature = false;
    private List<File> fileAttachments = null;
    private String fromDisplayName = "";
    private String fromAddress = "";
    private Environment env = null;
    
    public EmailUtil(Environment env) {
        this.env = env;
    }
    
    public EmailUtil toAddresses(List<String> addresses) {
        this.toAddresses = addresses;
        return this;
    }
    
    public EmailUtil fromDisplayName(String val) {
        fromDisplayName = val;
        return this;
    }
    
    public EmailUtil subject(String val) {
        subject = val;
        return this;
    }
    
    public EmailUtil htmlBody(String val) {
        htmlBody = val;
        return this;
    }
    
    public EmailUtil useSignature(Boolean bool) {
        useSignature = bool;
        return this;
    }
    
    public EmailUtil fromAddress(String val) {
        fromAddress = val;
        return this;
    }
    
    public EmailUtil fileAttachments(List<File> val) {
        fileAttachments = val;
        return this;
    }
    
    //where it all comes together
    //this method is private and is called from sendEmail()
    private EmailUtil build() throws AddressException, MessagingException {
        EmailOverrider overrider = new EmailOverrider(env);
        Session session = Session.getInstance(getProperties(), getAuthenticator(getProperties()));
        message = new MimeMessage(session);
        
        message.addRecipients(RecipientType.TO, overrider.getRecipients(toAddresses));
        message.setFrom(new InternetAddress(getProperties().getProperty("smtpFrom")));
        message.setSubject(this.subject);
        message.setSentDate(new Date());
        
        Multipart multipart = new MimeMultipart();
        
        multipart.addBodyPart(overrider.getBody(htmlBody, toAddresses));
        
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