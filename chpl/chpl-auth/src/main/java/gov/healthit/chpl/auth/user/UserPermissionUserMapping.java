package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.UserPermission;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
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
@Where(clause = "NOT deleted")
@AssociationOverrides({
    @AssociationOverride(name = "pk.user", joinColumns = @JoinColumn(name = "user_id", insertable = false, updatable = false)),
    @AssociationOverride(name = "pk.permission", joinColumns = @JoinColumn(name = "permission_id_user_permission", insertable = false, updatable = false)) })
public class UserPermissionUserMapping {

	
	@EmbeddedId
	private UserPermissionUserMappingPk pk = new UserPermissionUserMappingPk();
	
	@Column(name="deleted")
	private boolean deleted;
	
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public UserImpl getUserImpl() {
		return getPk().getUser();
	}

	public void setUserImpl(UserImpl user) {
		this.getPk().setUser(user);
	}

	public UserPermission getPermission() {
		return this.getPk().getPermission();
	}

	public void setPermission(UserPermission permission) {
		this.getPk().setPermission(permission);
	}
	
	public UserPermissionUserMappingPk getPk() {
		return pk;
	}

	public void setPk(UserPermissionUserMappingPk pk) {
		this.pk = pk;
	}
	
}
