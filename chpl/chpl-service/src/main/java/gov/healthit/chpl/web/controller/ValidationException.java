package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends Exception {
	private List<String> messages;
	
	private static final long serialVersionUID = 1L;
	public ValidationException() { 
		super(); 
		messages = new ArrayList<String>();
	}
	public ValidationException(String message) { 
		super(message);
		messages = new ArrayList<String>();
	}
	public ValidationException(String message, Throwable cause) { 
		super(message, cause); 
		messages = new ArrayList<String>();
	}
	public ValidationException(Throwable cause) { 
		super(cause);
		messages = new ArrayList<String>();
	}
	public ValidationException(List<String> messages) {
		super();
		this.messages = messages;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

}
