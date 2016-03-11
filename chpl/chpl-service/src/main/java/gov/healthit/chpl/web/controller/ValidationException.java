package gov.healthit.chpl.web.controller;

import java.util.HashSet;
import java.util.Set;

public class ValidationException extends Exception {
	private Set<String> errorMessages;
	private Set<String> warningMessages;
	
	private static final long serialVersionUID = 1L;
	public ValidationException() { 
		super(); 
		errorMessages = new HashSet<String>();
		warningMessages = new HashSet<String>();
	}
	public ValidationException(String message) { 
		super(message);
		errorMessages = new HashSet<String>();
		errorMessages.add(message);
		warningMessages = new HashSet<String>();
	}
	public ValidationException(String message, Throwable cause) { 
		super(message, cause); 
		errorMessages = new HashSet<String>();
		errorMessages.add(message);
		warningMessages = new HashSet<String>();
	}
	public ValidationException(Throwable cause) { 
		super(cause);
		errorMessages = new HashSet<String>();
		warningMessages = new HashSet<String>();
	}
	public ValidationException(Set<String> errorMessages, Set<String> warningMessages) {
		super();
		this.errorMessages = errorMessages;
		this.warningMessages = warningMessages;
	}
	public Set<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(Set<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public Set<String> getWarningMessages() {
		return warningMessages;
	}
	public void setWarningMessages(Set<String> warningMessages) {
		this.warningMessages = warningMessages;
	}


}
