package gov.healthit.chpl.auth.user;


import gov.healthit.chpl.auth.permission.JWTAuthenticatedPermission;
import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;







import org.springframework.security.core.GrantedAuthority;

public class JWTAuthenticatedUser implements User {
	
	
	private static final long serialVersionUID = 1L;
	private Long id;
	private String subjectName;
	private String password = null;
	private Set<UserPermission> permissions = new HashSet<UserPermission>();
	private final boolean accountExpired = false;
	private final boolean accountLocked = false;
	private final boolean credentialsExpired = false;
	private final boolean accountEnabled = true;
	private boolean authenticated = true;
	
	public JWTAuthenticatedUser(){
		this.subjectName = null;
		this.password = null;
	}
	
	public JWTAuthenticatedUser(String subjectName) {
		this.subjectName = subjectName;
		this.password = null;
	}
	
	public JWTAuthenticatedUser(String subjectName, String encodedPassword) {
		this.subjectName = subjectName;
		this.password = encodedPassword;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}
	
	public Set<UserPermission> getPermissions() {
		return this.permissions;
	}
	
	public void addPermission(UserPermission permission){
		this.permissions.add(permission);
	}
	
	public void addPermission(String permissionValue) {
		UserPermission permission = new JWTAuthenticatedPermission(permissionValue);
		this.permissions.add(permission);
	}

	@Override
	public void removePermission(String permissionValue){
		this.permissions.remove(new UserPermissionEntity(permissionValue));
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
	

}
