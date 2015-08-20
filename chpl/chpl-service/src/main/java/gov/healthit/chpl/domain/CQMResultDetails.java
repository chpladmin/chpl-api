package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CQMResultDetailsDTO;

public class CQMResultDetails {
	
	private String number;
	private String cmsId;
	private String title;
	private String nqfNumber;
	private Boolean success;
	private String version;
	
	public CQMResultDetails(){}
	
	public CQMResultDetails(CQMResultDetailsDTO dto){
		this.number = dto.getNumber();
		this.cmsId = dto.getCmsId();
		this.title = dto.getTitle();
		this.nqfNumber = dto.getNqfNumber();
		this.success = dto.getSuccess();
		this.version = dto.getVersion();
		
	}
	
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
	public String getNqfNumber() {
		return nqfNumber;
	}
	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
