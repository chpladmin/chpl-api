package gov.healthit.chpl.auth.authentication;
import gov.healthit.chpl.auth.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name="claim")
public class UserPermission implements GrantedAuthority {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="user_permission_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}

	@Column(name="authority")
	private String authority;
	
	@Column(name="description")
	private String description;
	
	@Column(name="last_modified_user")
	private Long lastModifiedUser;
	
	public UserPermission(){}
	
	public UserPermission(String authority){
		this.authority = authority;
		this.lastModifiedUser = Util.getCurrentUser().getId();
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
		this.lastModifiedUser = Util.getCurrentUser().getId();
	}
	
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.lastModifiedUser = Util.getCurrentUser().getId();
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public String getAuthority() {
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
	
	@Override
	public String toString(){
		return authority;
	}
	
}
