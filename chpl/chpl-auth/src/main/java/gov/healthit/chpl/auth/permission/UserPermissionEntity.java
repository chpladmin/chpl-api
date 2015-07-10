package gov.healthit.chpl.auth.permission;
import java.util.List;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name="user_permission")
@SQLDelete(sql = "UPDATE user_permission SET deleted = true WHERE user_permission_id = ?")
@Where(clause = "NOT deleted")
public class UserPermissionEntity implements UserPermission {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="user_permission_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;
	
	@Column(name="authority", unique=true)
	private String authority;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@OneToMany(mappedBy="pk.permission", fetch=FetchType.EAGER)
	private List<UserPermissionUserMapping> userMappings;

	public UserPermissionEntity(){
		this.lastModifiedUser = getLastModifiedUserId();
	}
	
	public UserPermissionEntity(String authority){
		this.authority = authority;
		this.name = authority;
		this.description = authority;
		this.lastModifiedUser = getLastModifiedUserId();
	}
	
	public UserPermissionEntity(String name, String authority){
		this.name = name;
		this.authority = authority;
		this.description = name;
		this.lastModifiedUser = getLastModifiedUserId();
	}
	
	public UserPermissionEntity(String name, String authority, String description){
		this.name = name;
		this.authority = authority;
		this.description = description;
		this.lastModifiedUser = getLastModifiedUserId();
	}	
	
	public Long getId() {
		return id;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
		populateLastModifiedUser();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		populateLastModifiedUser();
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public String getAuthority() {
		return authority;
	}
	
	public List<UserPermissionUserMapping> getUserMappings() {
		return userMappings;
	}

	public void setUserMappings(List<UserPermissionUserMapping> userMappings) {
		this.userMappings = userMappings;
		populateLastModifiedUser();
	}
	

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserPermission))
			return false;

		UserPermissionEntity claim = (UserPermissionEntity) obj;
		return claim.getAuthority() == this.getAuthority() || claim.getAuthority().equals(this.getAuthority());
	}

	@Override
	public int hashCode() {
		return getAuthority() == null ? 0 : getAuthority().hashCode();
	}
	
	@Override
	public String toString(){
		return authority;
	}
	
	private void populateLastModifiedUser(){
		User currentUser = Util.getCurrentUser();
		
		Long userId = new Long(-1);
		
		if (currentUser != null){
			userId = currentUser.getId();
		}
		this.lastModifiedUser = userId;
	}
	
	private static Long getLastModifiedUserId(){
		
		User currentUser = Util.getCurrentUser();
		
		Long userId = new Long(-1);
		
		if (currentUser != null){
			userId = currentUser.getId();
		}
		return userId;
	}
	
}