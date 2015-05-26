package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.authorization.Claim;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public interface User extends UserDetails , Authentication {
	
	public String getSubjectName();
	public void setSubjectName(String subject);
	public Set<Claim> getClaims();
	public void setClaims(Set<Claim> claims);
	public void addClaim(String claimValue);
	public void addClaim(Claim claim);
	public void removeClaim(String claimValue);
	public void removeClaim(Claim claim);
	
	
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
	
}
