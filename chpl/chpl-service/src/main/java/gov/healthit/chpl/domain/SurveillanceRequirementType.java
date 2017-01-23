package gov.healthit.chpl.domain;

import java.io.Serializable;

public class SurveillanceRequirementType implements Serializable {
	private static final long serialVersionUID = -5865384642096284604L;
	private Long id;
	private String name;
	
	public SurveillanceRequirementType() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
