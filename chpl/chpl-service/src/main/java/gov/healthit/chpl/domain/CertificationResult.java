package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

public class CertificationResult {
	
	
	private String number;
	private String title;
	private boolean success;
	
	public CertificationResult(){}
	public CertificationResult(CertificationResultDetailsDTO certResult) {
		this.setNumber(certResult.getNumber());
		this.setSuccess(certResult.getSuccess());
		this.setTitle(certResult.getTitle());	
	}
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean successful) {
		this.success = successful;
	}
	
}
