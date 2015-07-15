package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionUserMapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;


@Entity
@Table(name="`user`")
@SQLDelete(sql = "UPDATE openchpl.\"user\" SET deleted = true WHERE user_id = ?")
@Where(clause = "NOT deleted")
public class UserEntity implements User {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="user_name", unique=true)
	private String subjectName;
	
	@Column(name="password")
	private String password = null;
	
	@Column(name="account_expired")
	private boolean accountExpired;
	
	@Column(name="account_locked")
	private boolean accountLocked;
	
	@Column(name="credentials_expired")
	private boolean credentialsExpired;
	
	@Column(name="account_enabled")
	private boolean accountEnabled;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	@OneToMany(mappedBy="pk.user", fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
 	private Set<UserPermissionUserMapping> permissionMappings;
	
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="contact_id", unique=true, nullable=false, updatable=false)
	private UserContact contact;
	
	@Transient
	private boolean authenticated = false;
	
	public UserEntity(){
		this.subjectName = null;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	
	public UserEntity(String subjectName) {
		this.subjectName = subjectName;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	public UserEntity(String subjectName, String encodedPassword) {
		this.subjectName = subjectName;
		this.password = encodedPassword;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
		populateLastModifiedUser();
	}
	
	public String getFirstName(){
		return contact.getFirstName();
	}
	
	public void setFirstName(String firstName){
		contact.setFirstName(firstName);
	}
	
	public String getLastName(){
		return contact.getLastName();
	}
	
	public void setLastName(String lastName){
		contact.setLastName(lastName);
	}
	
	
	public Set<UserPermission> getPermissions() {
		
		Set<UserPermission> permissions = new HashSet<UserPermission>();
		
		for (UserPermissionUserMapping mapping : permissionMappings){
			permissions.add(mapping.getPermission());
		}
		return permissions;
	}
	
	public void addPermission(UserPermission permission) throws UserManagementException {
		
		UserPermissionEntity permissionEntity = (UserPermissionEntity) permission;
		
		UserPermissionUserMapping permissionMapping = new UserPermissionUserMapping();
		
		permissionMapping.setPermission(permissionEntity);
		permissionMapping.setUser(this);
		
		if (! this.permissionMappings.contains(permissionMapping)  ){
			System.out.println("Adding mapping to user.");
			this.permissionMappings.add(permissionMapping);
		} else {
			System.out.println("Mapping exists in user.");
			throw new UserManagementException("This user-permission mapping already exists");
		}
		
		if (! permissionEntity.getUserMappings().contains(permissionMapping)){
			permissionEntity.getUserMappings().add(permissionMapping);
		} else {
			throw new UserManagementException("This user-permission mapping already exists");
		}
		
	}

	public void removePermission(String permissionValue){
		this.permissionMappings.removeIf((UserPermissionUserMapping m) -> m.getPermission().getAuthority().equals(permissionValue));
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.getPermissions();
	}

	@Override
	public Object getCredentials() {
		return this.getPassword();
	}

	@Override
	public Object getDetails() {
		return this;
	}

	@Override
	public Object getPrincipal() {
		return this.subjectName;
	}

	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	@Override
	public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
		this.authenticated = arg0;
	}

	@Override
	public String getName() {
		return subjectName;
	}

	@Override
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String encodedPassword){
		this.password = encodedPassword;
		populateLastModifiedUser();
	}

	@Override
	public String getUsername() {
		return subjectName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return !accountExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}

	@Override
	public boolean isEnabled() {
		return accountEnabled;
	}

	public Long getId() {
		return id;
	}
	
	public UserContact getContact() {
		return contact;
	}
	
	public void setContact(UserContact contact) {
		this.contact = contact;
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
