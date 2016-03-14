package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;

public class CertifiedProductTargetedUser {
	private Long id;
	private Long targetedUserId;
	private String targetedUserName;

	public CertifiedProductTargetedUser() {
		super();
	}
	
	public CertifiedProductTargetedUser(CertifiedProductTargetedUserDTO dto) {
		this.id = dto.getId();
		this.targetedUserId = dto.getTargetedUserId();
		this.targetedUserName = dto.getTargetedUserName();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
