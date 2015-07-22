package gov.healthit.chpl.auth.json;

import java.util.ArrayList;
import java.util.List;

public class UserListObject {
	
	private List<UserInfoObject> users = new ArrayList<UserInfoObject>();

	public List<UserInfoObject> getUsers() {
		return users;
	}
	
	public void setUsers(List<UserInfoObject> users) {
		this.users = users;
	}
	
}
