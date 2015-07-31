package gov.healthit.chpl.json;

public class CQMResultJSONObject {
	
	private String number;
	private String cmsId;
	private String title;
	private String cqmDomain;
	private String nqfNumber;
	private String cqmVersion;
	private boolean success;
	
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getCmsId() {
		return cmsId;
	}
	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCqmDomain() {
		return cqmDomain;
	}
	public void setCqmDomain(String cqmDomain) {
		this.cqmDomain = cqmDomain;
	}
	public String getNqfNumber() {
		return nqfNumber;
	}
	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}
	public String getCqmVersion() {
		return cqmVersion;
	}
	public void setCqmVersion(String cqmVersion) {
		this.cqmVersion = cqmVersion;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
