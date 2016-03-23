package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductTargetedUserEntity;

public class PendingCertifiedProductTargetedUserDTO {
	private Long id;
	private Long pendingCertifiedProductId;
	private Long targetedUserId;
	private String name;
	
	public PendingCertifiedProductTargetedUserDTO() {}
	
	public PendingCertifiedProductTargetedUserDTO(PendingCertifiedProductTargetedUserEntity entity) {
		this.setId(entity.getId());
				
		if(entity.getMappedProduct() != null) {
			this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
		}
		this.setTargetedUserId(entity.getTargetedUserId());
		this.setName(entity.getName());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}
	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getTargetedUserId() {
		return targetedUserId;
	}

	public void setTargetedUserId(Long targetedUserId) {
		this.targetedUserId = targetedUserId;
	}
}
