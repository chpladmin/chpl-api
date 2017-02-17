package gov.healthit.chpl.auth;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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
 * Utility class for email sending
 * 
 * @author dlucas
 *
 */
@Service("SendMailUtil")
public class SendMailUtil {

	private static final Logger logger = LogManager.getLogger(SendMailUtil.class);
	@Autowired
	private Environment env;

	/**
	 * create and send an email with the provided string[] toEmails, subject and
	 * html message
	 * 
	 * @param invitation
	 */
	public void sendEmail(String[] toEmail, String subject, String htmlMessage)
			throws AddressException, MessagingException {
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

		logger.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
		logger.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
		logger.debug("Mail Username :" + env.getProperty("smtpUsername"));

		sendEmail(toEmail, subject, htmlMessage, properties);
	}

	/**
	 * Send an email with the given properties, as well as the string[] toEmail,
	 * subject, and html message
	 * 
	 * @param toEmail
	 * @param subject
	 * @param htmlMessage
	 * @param properties
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] toEmail, String subject, String htmlMessage, Properties properties)
			throws AddressException, MessagingException {
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
			logger.debug("Sending email from " + properties.getProperty("smtpFrom"));
		} catch (MessagingException ex) {
			logger.fatal("Invalid Email Address: " + properties.getProperty("smtpFrom"), ex);
			throw ex;
		}

		try {
			InternetAddress[] toAddresses = new InternetAddress[toEmail.length];
			for (int i = 0; i < toEmail.length; i++) {
				InternetAddress toEmailaddress = new InternetAddress(toEmail[i]);
				toAddresses[i] = toEmailaddress;
			}
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
			logger.debug("Sending email to " + Arrays.toString(toEmail));
		} catch (MessagingException ex) {
			logger.fatal("Invalid Email Address: " + Arrays.toString(toEmail), ex);
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
	 * Send an email with files, in addition to the toEmail, subject, and html
	 * message
	 * 
	 * @param toEmail
	 * @param subject
	 * @param htmlMessage
	 * @param files
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] toEmail, String subject, String htmlMessage, List<File> files)
			throws AddressException, MessagingException {
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

		logger.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
		logger.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
		logger.debug("Mail Username :" + env.getProperty("smtpUsername"));

		sendEmail(toEmail, subject, htmlMessage, files, properties);
	}

	/**
	 * Send an email using the given properties and list of files to attach.
	 * Also uses the toEmail, subject and htmlMessage
	 * 
	 * @param toEmail
	 * @param subject
	 * @param htmlMessage
	 * @param files
	 * @param props
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] toEmail, String subject, String htmlMessage, List<File> files, Properties props)
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

		logger.debug("Mail Host: " + properties.getProperty("mail.smtp.host"));
		logger.debug("Mail Port: " + properties.getProperty("mail.smtp.port"));
		logger.debug("Mail Username :" + props.getProperty("smtpUsername"));

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
			logger.debug("Sending email from " + props.getProperty("smtpFrom"));
		} catch (MessagingException ex) {
			logger.fatal("Invalid Email Address: " + props.getProperty("smtpFrom"), ex);
			throw ex;
		}

		try {
			InternetAddress[] toAddresses = new InternetAddress[toEmail.length];
			for (int i = 0; i < toEmail.length; i++) {
				InternetAddress toEmailaddress = new InternetAddress(toEmail[i]);
				toAddresses[i] = toEmailaddress;
			}
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
			logger.debug("Sending email to " + Arrays.toString(toEmail));
		} catch (MessagingException ex) {
			logger.fatal("Invalid Email Address: " + Arrays.toString(toEmail), ex);
			throw ex;
		}

		msg.setSubject(subject);
		msg.setSentDate(new Date());

		BodyPart messageBodyPartWithMessage = new MimeBodyPart();
		messageBodyPartWithMessage.setContent(htmlMessage, "text/html");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPartWithMessage);

		// Add file attachments to email
		for (File file : files) {
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file.getName());

			multipart.addBodyPart(messageBodyPart);
		}

		msg.setContent(multipart, "text/html");

		// sends the e-mail
		try {
			Transport.send(msg);
		} catch (SendFailedException e) {
			logger.info(e);
		}
	}

}
