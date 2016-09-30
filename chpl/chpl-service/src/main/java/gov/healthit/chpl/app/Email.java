package gov.healthit.chpl.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.SendMailUtil;

/**
 * Represents an email, its properties and methods
 * @author dlucas
 *
 */
@Component("email")
public class Email {
	@Autowired
	SendMailUtil sendMailUtil;
	private String[] emailTo;
	private String emailSubject;
	private String emailMessage;
	private Properties props;
	private List<File> files;

	public Email(){
	}
	
	/**
	 * Creates an email that contains one file.
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @param props - the Properties object to use for email creation
	 * @param file - the file to attach to the email
	 */
	public Email(String[] emailTo, String emailSubject, String emailMessage, Properties props, File file){
		setEmailTo(emailTo);
		setEmailSubject(emailSubject);
		setEmailMessage(emailMessage);
		setProps(props);
		List<File> files = new ArrayList<File>();
		files.add(file);
		setFiles(files);
	}
	
	/**
	 * Creates an email that contains multiple file attachments. 
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @param props - the Properties object to use for email creation
	 * @param files - the list of files to attach
	 */
	public Email(String[] emailTo, String emailSubject, String emailMessage, Properties props, List<File> files){
		setEmailTo(emailTo);
		setEmailSubject(emailSubject);
		setEmailMessage(emailMessage);
		setProps(props);
		setFiles(files);
	}
	
	/**
	 * Sends an email
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @param props - the Properties object to use for email creation
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] emailTo, String emailSubject, String emailMessage, Properties props) throws AddressException, MessagingException {
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage, props);
	}
	
	/**
	 * Sends an email
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @param props - the Properties object to use for email creation
	 * @param files - the list of files to attach to the email
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] emailTo, String emailSubject, String emailMessage, Properties props, List<File> files) throws AddressException, MessagingException{
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage, files, props);
	}
	
	/**
	 * Sends an email
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @param files - the list of files to attach to the email
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] emailTo, String emailSubject, String emailMessage, List<File> files) throws AddressException, MessagingException{
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage, files);
	}
	
	/**
	 * Sends an email
	 * @param emailTo - list of emails to use in the 'To' field of the email
	 * @param emailSubject - text subject of the email
	 * @param emailMessage - text message of the email
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendEmail(String[] emailTo, String emailSubject, String emailMessage) throws AddressException, MessagingException {
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage);
	}
	
	public SendMailUtil getSendMailUtil(){
		return this.sendMailUtil;
	}
	
	public void setSendMailUtil(SendMailUtil sendMailUtil){
		this.sendMailUtil = sendMailUtil;
	}
	
	public String getEmailSubject(){
		return this.emailSubject;
	}
	
	public void setEmailSubject(String emailSubject){
		this.emailSubject = emailSubject;
	}
	
	public String[] getEmailTo(){
		return this.emailTo;
	}
	
	public void setEmailTo(String[] emailTo){
		this.emailTo = emailTo;
	}
	
	public String getEmailMessage(){
		return this.emailMessage;
	}
	
	public void setEmailMessage(String emailMessage){
		this.emailMessage = emailMessage;
	}
	
	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}
}