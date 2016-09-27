package gov.healthit.chpl.app;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.SendMailUtil;

@Component("email")
public class Email {
	@Autowired
	SendMailUtil sendMailUtil;
	private String[] emailTo;
	private String emailSubject;
	private String emailMessage;
	private Properties props;

	public Email(){
	}
	
	public void sendSummaryEmail() throws AddressException, MessagingException {
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage, props);
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
}