package gov.healthit.chpl.auth.permission;


public class JWTAuthenticatedPermission implements UserPermission {
	
	private static final long serialVersionUID = 1L;
	
	private String authority;
	
	public JWTAuthenticatedPermission(String authority){
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
	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
