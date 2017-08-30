package gov.healthit.chpl.domain;

import java.io.Serializable;

public class ApiKey implements Serializable {
	private static final long serialVersionUID = -3412202704187626073L;
	private String name;
	private String email;
	private String key;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}	
	
}
