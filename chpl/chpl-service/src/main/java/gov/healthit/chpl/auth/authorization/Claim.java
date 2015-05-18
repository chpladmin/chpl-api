package gov.healthit.chpl.auth.authorization;
import org.springframework.security.core.GrantedAuthority;

public class Claim implements GrantedAuthority {
	
	private static final long serialVersionUID = 1L;
	
	private String authority;
	
	public Claim(){}
	
	public Claim(String authority){
		this.authority = authority;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	@Override
	public String getAuthority() {
		return authority;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Claim))
			return false;

		Claim claim = (Claim) obj;
		return claim.getAuthority() == this.getAuthority() || claim.getAuthority().equals(this.getAuthority());
	}

	@Override
	public int hashCode() {
		return getAuthority() == null ? 0 : getAuthority().hashCode();
	}

	
}
