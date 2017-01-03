package gov.healthit.chpl.domain;

import java.io.Serializable;

public class SurveillanceNonconformityStatus implements Serializable {
	private static final long serialVersionUID = -411041849666278903L;
	private Long id;
	private String name;
	
	public SurveillanceNonconformityStatus() {}

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
