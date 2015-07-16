package gov.healthit.chpl.auth.permission;


import javax.persistence.Column;

import org.springframework.security.core.GrantedAuthority;

public interface UserPermission extends GrantedAuthority {
	
	public String getAuthority();
	public void setAuthority(String authority);
	public String getName();
	public String getDescription();
	
}
