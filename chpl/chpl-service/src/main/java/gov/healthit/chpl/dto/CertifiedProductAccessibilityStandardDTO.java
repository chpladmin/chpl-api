package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertifiedProductAccessibilityStandardEntity;

public class CertifiedProductAccessibilityStandardDTO implements Serializable {
    private static final long serialVersionUID = -8092493683326428044L;
    private Long id;
    private Long certifiedProductId;
    private Long accessibilityStandardId;
    private String accessibilityStandardName;

    public CertifiedProductAccessibilityStandardDTO() {
    }

    public CertifiedProductAccessibilityStandardDTO(CertifiedProductAccessibilityStandardEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.accessibilityStandardId = entity.getAccessibilityStandardId();
        if (entity.getAccessibilityStandard() != null) {
            this.accessibilityStandardName = entity.getAccessibilityStandard().getName();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Long getAccessibilityStandardId() {
        return accessibilityStandardId;
    }

    public void setAccessibilityStandardId(final Long accessibilityStandardId) {
        this.accessibilityStandardId = accessibilityStandardId;
    }

    public String getAccessibilityStandardName() {
        return accessibilityStandardName;
    }

    public void setAccessibilityStandardName(final String accessibilityStandardName) {
        this.accessibilityStandardName = accessibilityStandardName;
    }

}
