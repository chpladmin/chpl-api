package gov.healthit.chpl.domain;

import gov.healthit.chpl.auth.authentication.LoginCredentials;

public class AuthorizeCredentials extends LoginCredentials {
	
	private String hash;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
