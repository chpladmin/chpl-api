package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductAccessibilityStandardEntity;

public class PendingCertifiedProductAccessibilityStandardDTO implements Serializable {
    private static final long serialVersionUID = 3848620066733249423L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Long accessibilityStandardId;
    private String name;
    private String fuzzyMatchAccessibilityStandardName;

    public PendingCertifiedProductAccessibilityStandardDTO() {
    }

    public PendingCertifiedProductAccessibilityStandardDTO(PendingCertifiedProductAccessibilityStandardEntity entity) {
        this.setId(entity.getId());

        if (entity.getMappedProduct() != null) {
            this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
        }
        this.setAccessibilityStandardId(entity.getAccessibilityStandardId());
        this.setName(entity.getName());
        this.setFuzzyMatchAccessibilityStandardName(entity.getFuzzyMatchName());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertifiedProductId() {
        return pendingCertifiedProductId;
    }

    public void setPendingCertifiedProductId(final Long pendingCertifiedProductId) {
        this.pendingCertifiedProductId = pendingCertifiedProductId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getAccessibilityStandardId() {
        return accessibilityStandardId;
    }

    public void setAccessibilityStandardId(final Long accessibilityStandardId) {
        this.accessibilityStandardId = accessibilityStandardId;
    }

	public String getFuzzyMatchAccessibilityStandardName() {
		return fuzzyMatchAccessibilityStandardName;
	}

	public void setFuzzyMatchAccessibilityStandardName(
			String fuzzyMatchAccessibilityStandardName) {
		this.fuzzyMatchAccessibilityStandardName = fuzzyMatchAccessibilityStandardName;
	}
}
