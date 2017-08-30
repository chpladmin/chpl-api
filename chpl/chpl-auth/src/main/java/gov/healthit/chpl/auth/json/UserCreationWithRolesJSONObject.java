package gov.healthit.chpl.auth.json;

import java.util.List;

public class UserCreationWithRolesJSONObject extends UserCreationJSONObject {
	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
}
