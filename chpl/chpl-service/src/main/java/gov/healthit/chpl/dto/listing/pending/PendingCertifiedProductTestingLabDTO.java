package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTestingLabMapEntity;

/**
 * Pending Certified Product - Testing Lab mapping DTO.
 * @author alarned
 *
 */
public class PendingCertifiedProductTestingLabDTO implements Serializable {
    private static final long serialVersionUID = 8450020346312080499L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Long testingLabId;
    private String testingLabName;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductTestingLabDTO() {
    }

    /**
     * Constructed from entity.
     * @param entity the entity
     */
    public PendingCertifiedProductTestingLabDTO(final PendingCertifiedProductTestingLabMapEntity entity) {
        this.setId(entity.getId());

        if (entity.getMappedProduct() != null) {
            this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
        }
        if (entity.getTestingLabId() != null) {
            this.setTestingLabId(entity.getTestingLabId());
        }
        if (entity.getTestingLabName() != null) {
            this.setTestingLabName(entity.getTestingLabName());
        }
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

    @Override
    public String toString() {
        return "PendingCertifiedProductTestingLabDTO [id=" + id + ", pendingCertifiedProductId="
                + pendingCertifiedProductId + ", testingLabId=" + testingLabId + ", testingLabName=" + testingLabName
                + "]";
    }
}
