package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;


public class CertificationResultDetailsDTO implements Serializable {
	private static final long serialVersionUID = 4560202421131481086L;
	private Long id;
    private Long certificationCriterionId;
    private Boolean success;
    private String number;
    private String title;
    private Boolean gap;
    private Boolean sed;
    private Boolean g1Success;
    private Boolean g2Success;
    private String apiDocumentation;
	private String privacySecurityFramework;
	
    public CertificationResultDetailsDTO(){}
    
    public CertificationResultDetailsDTO(CertificationResultDetailsEntity entity){
    	
    	this.id = entity.getId();
    	this.certificationCriterionId = entity.getCertificationCriterionId();
    	this.success = entity.getSuccess();
    	this.number = entity.getNumber();
    	this.title = entity.getTitle();
    	this.gap = entity.getGap();
    	this.sed = entity.getSed();
    	this.g1Success = entity.getG1Success();
    	this.g2Success = entity.getG2Success();
    	this.apiDocumentation = entity.getApiDocumentation();
    	this.privacySecurityFramework = entity.getPrivacySecurityFramework();
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

	public Boolean getGap() {
		return gap;
	}

	public void setGap(Boolean gap) {
		this.gap = gap;
	}

	public Boolean getSed() {
		return sed;
	}

	public void setSed(Boolean sed) {
		this.sed = sed;
	}

	public Boolean getG1Success() {
		return g1Success;
	}

	public void setG1Success(Boolean g1Success) {
		this.g1Success = g1Success;
	}

	public Boolean getG2Success() {
		return g2Success;
	}

	public void setG2Success(Boolean g2Success) {
		this.g2Success = g2Success;
	}

	public String getApiDocumentation() {
		return apiDocumentation;
	}

	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}

	public String getPrivacySecurityFramework() {
		return privacySecurityFramework;
	}

	public void setPrivacySecurityFramework(String privacySecurityFramework) {
		this.privacySecurityFramework = privacySecurityFramework;
	}
}
