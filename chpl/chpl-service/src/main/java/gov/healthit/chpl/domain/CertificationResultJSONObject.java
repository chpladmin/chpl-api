package gov.healthit.chpl.domain;

public class CertificationResultJSONObject {
	
	
	private String number;
	private String title;
	private boolean success;
	
	
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
