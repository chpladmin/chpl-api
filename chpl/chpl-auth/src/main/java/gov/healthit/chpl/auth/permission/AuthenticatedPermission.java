package gov.healthit.chpl.auth.permission;


public class AuthenticatedPermission implements UserPermission {
	
	private static final long serialVersionUID = 1L;
	
	private String authority;
	private String name;
	private String description;
	
	public AuthenticatedPermission(){}
	
	public AuthenticatedPermission(String authority){
		this.authority = authority;
	}
	
	@Override
	public String getAuthority() {
		return authority;
	}

	@Override
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString(){
		return authority;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserPermission))
			return false;

		UserPermission claim = (UserPermission) obj;
		return claim.getAuthority() == this.getAuthority() || claim.getAuthority().equals(this.getAuthority());
	}

	@Override
	public int hashCode() {
		return getAuthority() == null ? 0 : getAuthority().hashCode();
	}

}
