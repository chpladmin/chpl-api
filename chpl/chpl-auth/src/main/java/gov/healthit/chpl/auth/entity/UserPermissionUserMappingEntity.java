package gov.healthit.chpl.auth.entity;


import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;


@Entity
@Table(name="global_user_permission_map")
@SQLDelete(sql = "UPDATE openchpl.global_user_permission_map SET deleted = true WHERE global_user_permission_id = ?")
public class UserPermissionUserMappingEntity {
	
	@EmbeddedId
	private UserPermissionUserMappingPk pk = new UserPermissionUserMappingPk();
	
	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name="global_user_permission_id", columnDefinition="bigserial", insertable = false, updatable = false)
	private Long permissionMappingId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", updatable = false, insertable = false)
	private UserEntity user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_permission_id_user_permission", updatable = false, insertable = false)
	private UserPermissionEntity permission;
	
	
	public UserPermissionUserMappingEntity(){}
	
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public UserEntity getUser() {
		return user;
	}
	
	public void setUser(UserEntity user) {
		this.user = user;
		this.getPk().setUserId(user.getId());
	}
	
	public UserPermissionEntity getPermission() {
		return permission;
	}
	
	public void setPermission(UserPermissionEntity permission) {
		this.permission = permission;
		this.getPk().setPermissionId(permission.getId());
	}
	
	public UserPermissionUserMappingPk getPk() {
		return pk;
	}
	
	public void setPk(UserPermissionUserMappingPk pk) {
		this.pk = pk;
	}
	
	public Long getPermissionMappingId() {
		return permissionMappingId;
	}

	public void setPermissionMappingId(Long permissionMappingId) {
		this.permissionMappingId = permissionMappingId;
	}

	public int hashCode() {
		return (int)(getUser().getId() + getPermission().getId());
	}
	
	public boolean equals(Object object) {
		if (object instanceof UserPermissionUserMappingEntity) {
			UserPermissionUserMappingEntity other = (UserPermissionUserMappingEntity) object;
			return (other.getPk().equals(this.getPk()));
		}
		return false;
	}
	
}
