package gov.healthit.chpl.domain;

import gov.healthit.chpl.auth.json.UserCreationJSONObject;

public class CreateUserFromInvitationRequest {
	private String hash;
	private UserCreationJSONObject user;
	
	public UserCreationJSONObject getUser() {
		return user;
	}
	public void setUser(UserCreationJSONObject user) {
		this.user = user;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
}
