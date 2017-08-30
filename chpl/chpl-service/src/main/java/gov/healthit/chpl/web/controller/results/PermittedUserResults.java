package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.PermittedUser;

public class PermittedUserResults implements Serializable {
	private static final long serialVersionUID = -3222325462520840608L;
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
