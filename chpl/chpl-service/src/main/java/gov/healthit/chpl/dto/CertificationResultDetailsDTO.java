package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertificationResultDetailsEntity;


public class CertificationResultDetailsDTO {

	private Long id;
    private Long certificationCriterionId;
    private Boolean success;
    private String number;
    private String title;
    
    public CertificationResultDetailsDTO(){}
    
    public CertificationResultDetailsDTO(CertificationResultDetailsEntity entity){
    	
    	this.id = entity.getId();
    	this.certificationCriterionId = entity.getCertificationCriterionId();
    	this.success = entity.getSuccess();
    	this.number = entity.getNumber();
    	this.title = entity.getTitle();
    	
    }
    
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}
	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
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

}
