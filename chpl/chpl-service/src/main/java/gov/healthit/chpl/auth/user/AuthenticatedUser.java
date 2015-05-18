package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.authorization.Claim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;



public class AuthenticatedUser implements User {

	private static final long serialVersionUID = 1L;

	private String subjectName;
	
	private String password;
	
	private List<Claim> claims = new ArrayList<Claim>();
	
	private boolean accountExpired;
	
	private boolean accountLocked;
	
	private boolean credentialsExpired;
	
	private boolean accountEnabled;
	
	private boolean authenticated = true;
	
	public AuthenticatedUser(){};
	
	public AuthenticatedUser(String subjectName, List<Claim> claims){
		this.subjectName = subjectName;
		this.claims = claims;
	}
	
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subject) {
		this.subjectName = subject;
	}

	public List<Claim> getClaims() {
		return this.claims;
	}

	public void setClaims(List<Claim> claims) {
		this.claims = claims;
	}

	public void addClaim(String claimValue){
		this.claims.add(new  Claim(claimValue));
	}
	
	public void addClaim(Claim claim){
		this.claims.add(claim);
	}

	public void removeClaim(String claimValue) {
		
		Claim remove = new Claim(claimValue);
		claims.remove(remove);
		
	}
	
	@Override
	public void removeClaim(Claim claim) {
		claims.remove(claim);
	}
	

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.claims;
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
