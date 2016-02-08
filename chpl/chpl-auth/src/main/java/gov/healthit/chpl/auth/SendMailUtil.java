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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("SendMailUtil")
public class SendMailUtil {
	
	private static final Logger logger = LogManager.getLogger(SendMailUtil.class);
	@Autowired private Environment env;
	
	/**
	 * create and send the email to invite the user
	 * @param invitation
	 */
	public void sendEmail(String toEmail, String subject, String htmlMessage) throws AddressException, MessagingException {
		//do not attempt to send email if we are in a dev environment
		String mailHost = env.getProperty("smtpHost");
		if(StringUtils.isEmpty(mailHost) || "development".equalsIgnoreCase(mailHost) || "dev".equalsIgnoreCase(mailHost)) {
			return;
		}
		
		 // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", env.getProperty("smtpHost"));
        properties.put("mail.smtp.port", env.getProperty("smtpPort"));
        properties.put("mail.smtp.auth", "true");
        //properties.put("mail.smtp.starttls.enable", "true");
 
        logger.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
        logger.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
        logger.debug("Mail Username :" + env.getProperty("smtpUsername"));
        logger.debug("Mail Password: " + env.getProperty("smtpPassword"));
        
        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(env.getProperty("smtpUsername"), env.getProperty("smtpPassword"));
            }
        };
 
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        try 
        {
        	InternetAddress fromEmail = new InternetAddress(env.getProperty("smtpFrom"));
	        msg.setFrom(fromEmail);
	        logger.debug("Sending email from " + env.getProperty("smtpFrom"));
        } catch(MessagingException ex) {
        	logger.fatal("Invalid Email Address: " + env.getProperty("smtpFrom"), ex);
        	throw ex;
        }
        
        try {
        	InternetAddress toEmailaddress = new InternetAddress(toEmail);
	        InternetAddress[] toAddresses = { toEmailaddress };
	        msg.setRecipients(Message.RecipientType.TO, toAddresses);
	        logger.debug("Sending email to " + toEmail);
        } catch (MessagingException ex) {
        	logger.fatal("Invalid Email Address: " + toEmail, ex);
        	throw ex;
        }
	      
        msg.setSubject(subject);
	    msg.setSentDate(new Date());
	    // set plain text message
	    msg.setContent(htmlMessage, "text/html");
	
	    // sends the e-mail
	    Transport.send(msg);
	}
}
