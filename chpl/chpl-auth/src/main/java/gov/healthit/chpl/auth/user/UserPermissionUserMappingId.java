package gov.healthit.chpl.auth.user;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class UserPermissionUserMappingId implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	private Long userId;
	private Long permissionId;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public int hashCode() {
		return (int)(userId + permissionId);
	}
	
	public boolean equals(Object object) {
		if (object instanceof UserPermissionUserMappingId) {
			UserPermissionUserMappingId otherId = (UserPermissionUserMappingId) object;
			return (otherId.userId == this.userId) && (otherId.permissionId == this.permissionId);
		}
		return false;
	}
	
	public Long getPermissionId() {
		return permissionId;
	}
	
	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}
	
}
