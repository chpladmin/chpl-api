package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.PermittedUser;

public class PermittedUserResults {
	private List<PermittedUser> users;

	public PermittedUserResults() {
		users = new ArrayList<PermittedUser>();
	}

	public List<PermittedUser> getUsers() {
		return users;
	}

	public void setUsers(List<PermittedUser> users) {
		this.users = users;
	}
}
