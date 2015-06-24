package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.UserPermission;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="global_user_permission_map")
//@IdClass(UserPermissionUserMappingId.class)
//@SQLDelete(sql = "UPDATE global_user_permission_map SET deleted = true WHERE user_permission_id = ?")
@Where(clause = "deleted = false")
public class UserPermissionUserMapping {

	
	@EmbeddedId
	private UserPermissionUserMappingId pk = new UserPermissionUserMappingId();
	/*
	@Id
	@Column(name="user_id")
	private long userId;
	
	@Id
	@Column(name="permission_id_user_permission")
	private long permissionId;
	*/
	
	@Column(name="deleted")
	private boolean deleted;
	
	
	@ManyToOne
	//@PrimaryKeyJoinColumn(name="user_id", referencedColumnName="user_id")
	  /* if this JPA model doesn't create a table for the "PROJ_EMP" entity,
	  *  please comment out the @PrimaryKeyJoinColumn, and use the ff:
	  *  @JoinColumn(name = "employeeId", updatable = false, insertable = false)
	  * or @JoinColumn(name = "employeeId", updatable = false, insertable = false, referencedColumnName = "id")
	  */
	@JoinColumn(name = "user_id", updatable = false, insertable = false, referencedColumnName = "user_id")
	private UserImpl userImpl;
	 
	  
	@ManyToOne
	//@PrimaryKeyJoinColumn(name="permission_id_user_permission", referencedColumnName="user_permission_id")
	//@PrimaryKeyJoinColumn(name="user_permission_id_user_permission", referencedColumnName="user_permission_id")
	//@PrimaryKeyJoinColumn(name="user_permission_id_user_permission", referencedColumnName="user_permission_id")
	@JoinColumn(name = "permission_id_user_permission", updatable = false, insertable = false, referencedColumnName = "user_permission_id")
	private UserPermission permission;
	
	/*
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getPermissionId() {
		return permissionId;
	}
		
	public void setPermissionId(long permissionId) {
		this.permissionId = permissionId;
	}
	*/
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public UserImpl getUserImpl() {
		return userImpl;
	}

	public void setUserImpl(UserImpl user) {
		this.userImpl = user;
	}

	public UserPermission getPermission() {
		return permission;
	}

	public void setPermission(UserPermission permission) {
		this.permission = permission;
	}
	/*
	public UserPermissionUserMappingId getPk() {
		return pk;
	}

	public void setPk(UserPermissionUserMappingId pk) {
		this.pk = pk;
	}
	*/
}
