package gov.healthit.chpl.domain;

import java.util.List;

import gov.healthit.chpl.certifiedProduct.validation.ValidationStatus;

public class ValidationErrorJSONObject {
	private ValidationStatus validationStatus;
	private List<String> validationMessages;
	public ValidationStatus getValidationStatus() {
		return validationStatus;
	}
	public void setValidationStatus(ValidationStatus validationStatus) {
		this.validationStatus = validationStatus;
	}
	public List<String> getValidationMessages() {
		return validationMessages;
	}
	public void setValidationMessages(List<String> validationMessages) {
		this.validationMessages = validationMessages;
	}
}
