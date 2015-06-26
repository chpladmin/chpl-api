package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.permission.UserPermission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public interface User extends UserDetails , Authentication {
	
	public String getSubjectName();
	public void setSubjectName(String subject);
	public Set<UserPermission> getPermissions();
	//public void setPermissions(Set<UserPermission> permissions);
	public void addPermission(UserPermission permission);
	public void removePermission(String permissionValue);
	public void removePermission(UserPermission permission);
	
	
	// UserDetails interface
	@Override
	public String getPassword();

	@Override
	public String getUsername();

	@Override
	public boolean isAccountNonExpired();

	@Override
	public boolean isAccountNonLocked();

	@Override
	public boolean isCredentialsNonExpired();

	@Override
	public boolean isEnabled();
	
	// Authentication Interface
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities();

	@Override
	public Object getCredentials();

	@Override
	public Object getDetails();

	@Override
	public Object getPrincipal();
	
	@Override
	public boolean isAuthenticated();

	@Override
	public void setAuthenticated(boolean arg0) throws IllegalArgumentException;

	@Override
	public String getName();
	
	public Long getId();
	
}
