package gov.healthit.chpl.auth.permission;

import gov.healthit.chpl.auth.user.UserEntity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class UserPermissionUserMappingPk implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id", updatable = false, insertable = false)
	private UserEntity user;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "user_permission_id_user_permission", updatable = false, insertable = false)
	private UserPermissionEntity permission;
	
	public int hashCode() {
		return (int)(user.getId() + permission.getId());
	}
	
	public boolean equals(Object object) {
		if (object instanceof UserPermissionUserMappingPk) {
			UserPermissionUserMappingPk otherPk = (UserPermissionUserMappingPk) object;
			return (otherPk.getUser() == this.getUser()) && (otherPk.getPermission() == this.getPermission());
		}
		return false;
	}
	
	public UserPermissionEntity getPermission() {
		return permission;
	}
	
	public void setPermission(UserPermissionEntity permission) {
		this.permission = permission;
	}
	
	public UserEntity getUser() {
		return user;
	}
	
	public void setUser(UserEntity user) {
		this.user = user;
	}
	
}
