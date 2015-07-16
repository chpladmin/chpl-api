package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.AuthenticatedPermission;
import gov.healthit.chpl.auth.permission.UserPermission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class DatabaseAuthenticatedUser implements User {

	
	private static final long serialVersionUID = 1L;
	private Long id;
	private String subjectName;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String title;
	private Set<UserPermission> permissions = new HashSet<UserPermission>();
	private boolean accountExpired;
	private boolean accountLocked;
	private boolean credentialsExpired;
	private boolean accountEnabled;
	private boolean authenticated;	
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Set<UserPermission> getPermissions() {
		return this.permissions;
	}
	
	public void addPermission(UserPermission permission){
		this.permissions.add(permission);
	}
	
	public void addPermission(String permissionValue) {
		UserPermission permission = new AuthenticatedPermission(permissionValue);
		this.permissions.add(permission);
	}

	@Override
	public void removePermission(String permissionValue){
		this.permissions.remove(new AuthenticatedPermission(permissionValue));
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
		return null;
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


}
