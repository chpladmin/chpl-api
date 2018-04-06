package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertifiedProductTestingLabMapEntity;

/**
 * Certified Product - Testing Lab mapping DTO.
 * @author alarned
 *
 */
public class CertifiedProductTestingLabDTO implements Serializable {
    private static final long serialVersionUID = -7651077841236092973L;
    private Long id;
    private Long certifiedProductId;
    private Long testingLabId;
    private String testingLabName;
    private String testingLabCode;

    /**
     * Default constructor.
     */
    public CertifiedProductTestingLabDTO() {
    }

    /**
     * Constructor with entity.
     * @param entity the input
     */
    public CertifiedProductTestingLabDTO(final CertifiedProductTestingLabMapEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.testingLabId = entity.getTestingLabId();
        if (entity.getTestingLab() != null) {
            this.testingLabName = entity.getTestingLab().getName();
            this.testingLabCode = entity.getTestingLab().getTestingLabCode();
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

    public Long getTestingLabId() {
        return testingLabId;
    }

    public void setTestingLabId(final Long testingLabId) {
        this.testingLabId = testingLabId;
    }

    public String getTestingLabName() {
        return testingLabName;
    }

    public void setTestingLabName(final String testingLabName) {
        this.testingLabName = testingLabName;
    }

    public String getTestingLabCode() {
        return testingLabCode;
    }

    public void setTestingLabCode(final String testingLabCode) {
        this.testingLabCode = testingLabCode;
    }

}
