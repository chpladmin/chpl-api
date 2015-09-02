package gov.healthit.chpl.auth.json;

import gov.healthit.chpl.auth.dto.UserDTO;


public class UserInfoJSONObject {
	
	private User user;
	
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

}
