package gov.healthit.chpl.domain;

import java.io.Serializable;

public enum PrivacyAndSecurityFrameworkConcept implements Serializable {
	APPROACH_1("Approach 1"),
	APPROACH_2("Approach 2"),
	APPROACH_1_AND_2("Approach 1;Approach 2");
	
	private String name;
	private PrivacyAndSecurityFrameworkConcept(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
