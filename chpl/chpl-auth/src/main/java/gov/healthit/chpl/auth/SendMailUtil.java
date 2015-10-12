package gov.healthit.chpl.auth;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.SMTPAddressFailedException;

public class SendMailUtil extends AuthPropertiesConsumer {
	
	private static final Logger logger = LogManager.getLogger(SendMailUtil.class);

	/**
	 * create and send the email to invite the user
	 * @param invitation
	 */
	public void sendEmail(String toEmail, String htmlMessage) throws AddressException, MessagingException {
		 // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", getProps().getProperty("smtpHost"));
        properties.put("mail.smtp.port", getProps().getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
 
        logger.error(properties.getProperty("mail.smtp.host"));
        logger.error(properties.getProperty("mail.smtp.port"));
        logger.error(properties.getProperty("mail.smtp.auth"));

        logger.error(getProps().getProperty("smtpUsername"));
        logger.error(getProps().getProperty("smtpPassword"));
        
        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getProps().getProperty("smtpUsername"), getProps().getProperty("smtpPassword"));
            }
        };
 
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        try 
        {
	        msg.setFrom(new InternetAddress(getProps().getProperty("smtpUsername") + "@ainq.com"));
	        InternetAddress[] toAddresses = { new InternetAddress(toEmail) };
	        msg.setRecipients(Message.RecipientType.TO, toAddresses);
	        msg.setSubject("CHPL Invitation");
	        msg.setSentDate(new Date());
	        // set plain text message
	        msg.setContent(htmlMessage, "text/html");
	
	        // sends the e-mail
	        Transport.send(msg);
        }
        catch(SMTPAddressFailedException ex) {
        	logger.fatal("SMTP Address Failed!", ex);
        }
        catch(MessagingException ex) {
        	logger.fatal("Messaging Exception!", ex);
        	throw ex;
        }
	}
}
