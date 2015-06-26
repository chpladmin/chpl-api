package gov.healthit.chpl.auth.permission;


import org.springframework.security.core.GrantedAuthority;

public interface UserPermission extends GrantedAuthority {
	
	public String getAuthority();
	public void setAuthority(String authority);
	
}
