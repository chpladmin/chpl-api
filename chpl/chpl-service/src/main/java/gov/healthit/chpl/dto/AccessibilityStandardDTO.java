package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.AccessibilityStandardEntity;

public class AccessibilityStandardDTO implements Serializable {
	private static final long serialVersionUID = 4987850364061817190L;
	private Long id;
	private String name;

	public AccessibilityStandardDTO() {}

	public AccessibilityStandardDTO(AccessibilityStandardEntity entity) {
		this.id = entity.getId();
		this.name = entity.getName();
	}

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
