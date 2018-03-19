package gov.healthit.chpl.dto;

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
        this.setTestingLabId(entity.getTestingLabId());
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
}
