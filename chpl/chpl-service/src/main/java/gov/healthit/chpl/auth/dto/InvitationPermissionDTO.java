package gov.healthit.chpl.auth.dto;

import gov.healthit.chpl.auth.entity.InvitationPermissionEntity;

public class InvitationPermissionDTO {	
	private Long id;
	private Long userId;
	private Long permissionId;
	private String permissionName;
	
	public InvitationPermissionDTO() {}
	
	public InvitationPermissionDTO(InvitationPermissionEntity entity) {
		this.id = entity.getId();
		this.userId = entity.getInvitedUser().getId();
		this.permissionId = entity.getUserPermissionId();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}

	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
