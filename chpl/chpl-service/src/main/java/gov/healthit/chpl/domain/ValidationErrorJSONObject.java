package gov.healthit.chpl.domain;

import java.util.List;

public class ValidationErrorJSONObject {
	private List<String> errorMessages;
	private List<String> warningMessages;
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
