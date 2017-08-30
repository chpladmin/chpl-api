package gov.healthit.chpl.auth.entity;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserPermissionUserMappingPk implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "user_id", updatable = false, insertable = false)
	private Long userId;
	
	@Column(name = "user_permission_id_user_permission", updatable = false, insertable = false)
	private Long permissionId;
	
	
	public int hashCode() {
		return (int)(userId + permissionId);
	}
	
	public boolean equals(Object object) {
		if (object instanceof UserPermissionUserMappingPk) {
			UserPermissionUserMappingPk otherPk = (UserPermissionUserMappingPk) object;
			return (otherPk.getUserId() == this.getUserId()) && (otherPk.getPermissionId() == this.getPermissionId());
		}
		return false;
	}
	
	public Long getPermissionId() {
		return permissionId;
	}
	
	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
