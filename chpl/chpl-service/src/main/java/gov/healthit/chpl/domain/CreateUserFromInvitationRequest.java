package gov.healthit.chpl.domain;

import gov.healthit.chpl.auth.json.UserCreationWithRolesJSONObject;

public class CreateUserAndAddToAcbRequest {
	private Long acbId;
	private UserCreationWithRolesJSONObject user;
	private CertificationBodyPermission authority;
	
	public Long getAcbId() {
		return acbId;
	}
	public void setAcbId(Long acbId) {
		this.acbId = acbId;
	}

	public CertificationBodyPermission getAuthority() {
		return authority;
	}
	public void setAuthority(CertificationBodyPermission authority) {
		this.authority = authority;
	}
	public UserCreationWithRolesJSONObject getUser() {
		return user;
	}
	public void setUser(UserCreationWithRolesJSONObject user) {
		this.user = user;
	}
	
}
