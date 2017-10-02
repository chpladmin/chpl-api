package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.PendingCertifiedProductAccessibilityStandardEntity;

public class PendingCertifiedProductAccessibilityStandardDTO implements Serializable {
	private static final long serialVersionUID = 3848620066733249423L;
	private Long id;
	private Long pendingCertifiedProductId;
	private Long accessibilityStandardId;
	private String name;

	public PendingCertifiedProductAccessibilityStandardDTO() {}

	public PendingCertifiedProductAccessibilityStandardDTO(PendingCertifiedProductAccessibilityStandardEntity entity) {
		this.setId(entity.getId());

		if(entity.getMappedProduct() != null) {
			this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
		}
		this.setAccessibilityStandardId(entity.getAccessibilityStandardId());
		this.setName(entity.getName());
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}
	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getAccessibilityStandardId() {
		return accessibilityStandardId;
	}

	public void setAccessibilityStandardId(Long accessibilityStandardId) {
		this.accessibilityStandardId = accessibilityStandardId;
	}
}
