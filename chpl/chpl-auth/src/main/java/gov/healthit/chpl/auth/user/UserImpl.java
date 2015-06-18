package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.UserPermission;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;


@Entity
@Table(name="`user`")
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
	
	@ManyToMany(cascade = {CascadeType.ALL}, 
			fetch=FetchType.EAGER)
	@JoinTable(
			name="global_user_permission_map",
			joinColumns={@JoinColumn(name="user_id")},
			inverseJoinColumns={@JoinColumn(name="user_permission_id_user_permission")}
			)
	private Set<UserPermission> permissions = new HashSet<UserPermission>();
	
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
		return this.permissions;
	}

	public void setPermissions(Set<UserPermission> permissions) {
		this.permissions = permissions;
		populateLastModifiedUser();
	}
	
	public void addPermission(UserPermission permission){
		this.permissions.add(permission);
		populateLastModifiedUser();
	}

	public void removePermission(String permissionValue) {
		
		UserPermission remove = new UserPermission(permissionValue);
		permissions.remove(remove);
		populateLastModifiedUser();
	}
	
	@Override
	public void removePermission(UserPermission permission) {
		permissions.remove(permission);
		populateLastModifiedUser();
	}
	

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.permissions;
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
