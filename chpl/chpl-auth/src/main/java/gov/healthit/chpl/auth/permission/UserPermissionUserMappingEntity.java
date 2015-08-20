package gov.healthit.chpl.auth.permission;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserEntity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="global_user_permission_map")
@SQLDelete(sql = "UPDATE openchpl.global_user_permission_map SET deleted = true WHERE global_user_permission_id = ?")
@Where(clause = "NOT deleted")
public class UserPermissionUserMappingEntity {
	
	@EmbeddedId
	private UserPermissionUserMappingPk pk = new UserPermissionUserMappingPk();
	
	@Column(name="deleted")
	private boolean deleted;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@Column(name="global_user_permission_id", columnDefinition="bigserial", insertable = false, updatable = false)
	private Long permissionMappingId;
	
	
	public UserPermissionUserMappingEntity(){
		populateLastModifiedUser();
	}
	
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
		populateLastModifiedUser();
	}

	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
		populateLastModifiedUser();
	}

	public UserEntity getUser() {
		return getPk().getUser();
	}
	
	public void setUser(UserEntity user) {
		this.getPk().setUser(user);
		populateLastModifiedUser();
	}
	
	public UserPermissionEntity getPermission() {
		return this.getPk().getPermission();
	}
	
	public void setPermission(UserPermissionEntity permission) {
		this.getPk().setPermission(permission);
		populateLastModifiedUser();
	}
	
	public UserPermissionUserMappingPk getPk() {
		return pk;
	}
	
	public void setPk(UserPermissionUserMappingPk pk) {
		this.pk = pk;
		populateLastModifiedUser();
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
	
	
	private void populateLastModifiedUser(){
		User currentUser = Util.getCurrentUser();
		
		Long userId = new Long(-1);
		
		if (currentUser != null){
			userId = currentUser.getId();
		}
		this.lastModifiedUser = userId;
	}
	
}
