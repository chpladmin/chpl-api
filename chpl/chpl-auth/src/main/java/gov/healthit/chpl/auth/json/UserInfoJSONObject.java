package gov.healthit.chpl.auth.json;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserDTO;


public class UserInfoJSONObject {
	
	private User user;
	private List<String> permissions;
	
	public UserInfoJSONObject(){}
	
	public UserInfoJSONObject(UserDTO dto){
		this.user = new User(dto);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

}
