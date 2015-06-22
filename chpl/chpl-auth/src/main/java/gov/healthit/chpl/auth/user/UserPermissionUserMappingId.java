package gov.healthit.chpl.auth.user;

public class UserPermissionUserMappingId {
	
	private Long userId;
	private Long userPermissionId;
	
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getUserPermissionId() {
		return userPermissionId;
	}
	public void setUserPermissionId(Long userPermissionId) {
		this.userPermissionId = userPermissionId;
	}
	
	public int hashCode() {
		return (int)(userId + userPermissionId);
	}
	
	public boolean equals(Object object) {
		if (object instanceof UserPermissionUserMappingId) {
			UserPermissionUserMappingId otherId = (UserPermissionUserMappingId) object;
			return (otherId.userId == this.userId) && (otherId.userPermissionId == this.userPermissionId);
		}
		return false;
	}
	
}
