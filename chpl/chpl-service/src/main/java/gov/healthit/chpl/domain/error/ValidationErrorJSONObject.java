package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.Collection;

public class ValidationErrorJSONObject implements Serializable {
	private static final long serialVersionUID = -2186304674032903240L;
	private Collection<String> errorMessages;
	private Collection<String> warningMessages;
	public Collection<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(Collection<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public Collection<String> getWarningMessages() {
		return warningMessages;
	}
	public void setWarningMessages(Collection<String> warningMessages) {
		this.warningMessages = warningMessages;
	}
	
}
