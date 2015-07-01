package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.PermissionMappingManager;
import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
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
public class UserEntity extends BaseDBAuthenticatedUser implements User {

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
	
	public Set<UserPermission> getPermissions() {
		
		return getPermissionMappingManager().getPermissions(this);
	}
	
	public void addPermission(UserPermission permission){

		try {
			getPermissionMappingManager().grant(this, permission.getAuthority());
		} catch (UserPermissionRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removePermission(String permissionValue){
	
		try {
			getPermissionMappingManager().revoke(this, permissionValue);
		} catch (UserPermissionRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void removePermission(UserPermission permission) {
		
		try {
			getPermissionMappingManager().revoke(this, permission.getAuthority());
		} catch (UserPermissionRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
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
