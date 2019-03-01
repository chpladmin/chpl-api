package gov.healthit.chpl.auth.entity;
import java.util.List;

import javax.persistence.CascadeType;
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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.User;


@Entity
@Table(name="user_permission")
@SQLDelete(sql = "UPDATE user_permission SET deleted = true WHERE user_permission_id = ?")
@Where(clause = "NOT deleted")
public class UserPermissionEntity {
	
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

	@OneToMany(mappedBy="permission", fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserPermissionUserMappingEntity> userMappings;

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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	


	public List<UserPermissionUserMappingEntity> getUserMappings() {
		return userMappings;
	}

	public void setUserMappings(List<UserPermissionUserMappingEntity> userMappings) {
		this.userMappings = userMappings;
		populateLastModifiedUser();
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