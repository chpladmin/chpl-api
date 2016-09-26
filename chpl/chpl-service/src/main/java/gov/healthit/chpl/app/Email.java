package gov.healthit.chpl.app;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.SendMailUtil;

public class Email {
	@Autowired
	SendMailUtil sendMailUtil;
	String[] emailTo = {"dlucas@ainq.com"};
	String emailSubject;
	String emailMessage;
	
	public Email(){
	}
	
	public void sendSummaryEmail() throws AddressException, MessagingException {
		sendMailUtil.sendEmail(emailTo, emailSubject, emailMessage);
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
	
}