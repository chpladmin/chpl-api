package gov.healthit.chpl.auth.permission;

import org.springframework.security.core.GrantedAuthority;


public class GrantedPermission implements GrantedAuthority {
	
	private static final long serialVersionUID = 1L;
	
	private String authority;
	
	public GrantedPermission(String authority){
		this.authority = authority;
	}
	
	@Override
	public String getAuthority() {
		return authority;
	}
	
	@Override
	public String toString(){
		return authority;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GrantedPermission))
			return false;

		GrantedPermission claim = (GrantedPermission) obj;
		return claim.getAuthority() == this.getAuthority() || claim.getAuthority().equals(this.getAuthority());
	}

	@Override
	public int hashCode() {
		return getAuthority() == null ? 0 : getAuthority().hashCode();
	}
	
}
