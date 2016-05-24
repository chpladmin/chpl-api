package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.CertifiedProductTargetedUserEntity;

public class CertifiedProductAccessibilityStandardDTO {
	private Long id;
	private Long certifiedProductId;
	private Long accessibilityStandardId;
	private String accessibilityStandardName;
	
	public CertifiedProductAccessibilityStandardDTO(){}
	
	public CertifiedProductAccessibilityStandardDTO(CertifiedProductAccessibilityStandardEntity entity){
		this.id = entity.getId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.accessibilityStandardId = entity.getAccessibilityStandardId();
		if(entity.getAccessibilityStandard() != null) {
			this.accessibilityStandardName = entity.getAccessibilityStandard().getName();
		}
	}

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Long getAccessibilityStandardId() {
		return accessibilityStandardId;
	}

	public void setAccessibilityStandardId(Long accessibilityStandardId) {
		this.accessibilityStandardId = accessibilityStandardId;
	}

	public String getAccessibilityStandardName() {
		return accessibilityStandardName;
	}

	public void setAccessibilityStandardName(String accessibilityStandardName) {
		this.accessibilityStandardName = accessibilityStandardName;
	}

}
