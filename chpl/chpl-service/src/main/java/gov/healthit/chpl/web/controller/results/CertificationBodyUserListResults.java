package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBodyUser;

public class CertificationBodyUserListResults {
	private List<CertificationBodyUser> users;

	public CertificationBodyUserListResults() {
		users = new ArrayList<CertificationBodyUser>();
	}

	public List<CertificationBodyUser> getUsers() {
		return users;
	}

	public void setUsers(List<CertificationBodyUser> users) {
		this.users = users;
	}
	

	
}
