package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.UserPermission;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
public class UserImpl implements User {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id = new Long(-1);

	@Column(name="user_name", unique=true)
	private String subjectName;
	
	@Column(name="password")
	private String password = null;
	
	/*
	@ManyToMany(cascade = {CascadeType.ALL}, 
			fetch=FetchType.EAGER)
	@JoinTable(
			name="global_user_permission_map",
			joinColumns={@JoinColumn(name="user_id")},
			inverseJoinColumns={@JoinColumn(name="user_permission_id_user_permission")}
			)
	private Set<UserPermission> permissions = new HashSet<UserPermission>();
	*/
	@OneToMany(mappedBy="pk.user", fetch=FetchType.EAGER)
	private Set<UserPermissionUserMapping> permissionMappings;
	
	
	
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
	
	
	@Transient
	private boolean authenticated = false;
	
	public UserImpl(){
		this.subjectName = null;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	/*
	public UserImpl(String subjectName, Set<UserPermission> permissions) {
		this.subjectName = subjectName;
		this.permissions = permissions;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	*/
	
	/*
	public UserImpl(String subjectName, String encodedPassword, Set<UserPermission> permissions) {
		this.subjectName = subjectName;
		this.permissions = permissions;
		this.password = encodedPassword;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	*/
	
	public UserImpl(String subjectName) {
		this.subjectName = subjectName;
		this.password = null;
		this.accountExpired = false;
		this.accountLocked = false;
		this.credentialsExpired = false;
		this.accountEnabled = true;
		populateLastModifiedUser();
	}
	
	public UserImpl(String subjectName, String encodedPassword) {
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
	
	public Set<UserPermission> getPermissions() {
		
		Set<UserPermission> permissions = new HashSet<UserPermission>();
		
		for (UserPermissionUserMapping permMapping : permissionMappings){
			permissions.add(permMapping.getPermission());
		}
		return permissions;
	}

	public void setPermissions(Set<UserPermission> permissions) {
		
		
		for (UserPermission perm : permissions){
			this.addPermission(perm);
		}
		populateLastModifiedUser();
		
	}
	
	public void addPermission(UserPermission permission){
		
		UserPermissionUserMapping permMapping = new UserPermissionUserMapping();
		//permMapping.setPermissionId(permission.getId());
		permMapping.setPermission(permission);
		//permMapping.setUserId(this.id);
		permMapping.setUserImpl(this);
		
		this.permissionMappings.add(permMapping);
		populateLastModifiedUser();
	}

	public void removePermission(String permissionValue) {
		
		UserPermission remove = new UserPermission(permissionValue);
		this.removePermission(remove);
		
	}
	
	@Override
	public void removePermission(UserPermission permission) {
		
		for (UserPermissionUserMapping permMapping : this.permissionMappings){
			
			if (permMapping.getPermission().equals(permission)){
				this.permissionMappings.remove(permMapping);
			}
		}
		populateLastModifiedUser();
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
		return this.getName();
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
	
	private void populateLastModifiedUser(){
		User currentUser = Util.getCurrentUser();
		
		Long userId = new Long(-1);
		
		if (currentUser != null){
			userId = currentUser.getId();
		}
		this.lastModifiedUser = userId;
	}
	
}
