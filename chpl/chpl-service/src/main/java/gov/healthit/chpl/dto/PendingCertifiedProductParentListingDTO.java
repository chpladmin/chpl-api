package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductParentListingEntity;

public class PendingCertifiedProductParentListingDTO implements Serializable {
    private static final long serialVersionUID = 6805740521633648570L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Long parentListingId;
    private String parentListingUniqueId;

    public PendingCertifiedProductParentListingDTO() {
    }

    public PendingCertifiedProductParentListingDTO(PendingCertifiedProductParentListingEntity entity) {
        this.setId(entity.getId());

        if (entity.getMappedProduct() != null) {
            this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
        }

        this.parentListingId = entity.getParentListingId();
        this.parentListingUniqueId = entity.getParentListingUniqueId();
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

    public Long getParentListingId() {
        return parentListingId;
    }

    public void setParentListingId(Long parentListingId) {
        this.parentListingId = parentListingId;
    }

    public String getParentListingUniqueId() {
        return parentListingUniqueId;
    }

    public void setParentListingUniqueId(String parentListingUniqueId) {
        this.parentListingUniqueId = parentListingUniqueId;
    }
}
