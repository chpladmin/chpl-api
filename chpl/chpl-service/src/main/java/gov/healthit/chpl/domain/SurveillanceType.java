package gov.healthit.chpl.domain;

import java.io.Serializable;

public class SurveillanceType implements Serializable {
	private static final long serialVersionUID = 5788880200952752783L;
	private Long id;
	private String name;
	
	public SurveillanceType() {}

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
