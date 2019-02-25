package gov.healthit.chpl.auth.json;

import java.util.ArrayList;
import java.util.List;

public class UserListJSONObject {
	
	private List<UserInfoJSONObject> users = new ArrayList<UserInfoJSONObject>();

	public List<UserInfoJSONObject> getUsers() {
		return users;
	}
	
	public void setUsers(List<UserInfoJSONObject> users) {
		this.users = users;
	}
	
}
