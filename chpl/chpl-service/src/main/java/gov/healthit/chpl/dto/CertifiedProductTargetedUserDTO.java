package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CertifiedProductTargetedUserEntity;

public class CertifiedProductTargetedUserDTO {
	private Long id;
	private Long certifiedProductId;
	private Long targetedUserId;
	private String targetedUserName;
	
	public CertifiedProductTargetedUserDTO(){}
	
	public CertifiedProductTargetedUserDTO(CertifiedProductTargetedUserEntity entity){
		this.id = entity.getId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.targetedUserId = entity.getTargetedUserId();
		if(entity.getTargetedUser() != null) {
			this.targetedUserName = entity.getTargetedUser().getName();
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

	public Long getTargetedUserId() {
		return targetedUserId;
	}

	public void setTargetedUserId(Long targetedUserId) {
		this.targetedUserId = targetedUserId;
	}

	public String getTargetedUserName() {
		return targetedUserName;
	}

	public void setTargetedUserName(String targetedUserName) {
		this.targetedUserName = targetedUserName;
	}
}
