package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends Exception {
	private List<String> errorMessages;
	private List<String> warningMessages;
	
	private static final long serialVersionUID = 1L;
	public ValidationException() { 
		super(); 
		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();
	}
	public ValidationException(String message) { 
		super(message);
		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();
	}
	public ValidationException(String message, Throwable cause) { 
		super(message, cause); 
		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();
	}
	public ValidationException(Throwable cause) { 
		super(cause);
		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();
	}
	public ValidationException(List<String> errorMessages, List<String> warningMessages) {
		super();
		this.errorMessages = errorMessages;
		this.warningMessages = warningMessages;
	}
	public List<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public List<String> getWarningMessages() {
		return warningMessages;
	}
	public void setWarningMessages(List<String> warningMessages) {
		this.warningMessages = warningMessages;
	}


}
