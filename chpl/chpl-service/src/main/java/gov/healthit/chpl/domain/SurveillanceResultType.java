package gov.healthit.chpl.domain;

import java.io.Serializable;

public class SurveillanceResultType implements Serializable {
	private static final long serialVersionUID = 120064764043803388L;
	private Long id;
	private String name;
	
	public SurveillanceResultType() {}

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
