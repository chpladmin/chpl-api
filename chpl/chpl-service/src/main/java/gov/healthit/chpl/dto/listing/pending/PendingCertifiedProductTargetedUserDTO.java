package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTargetedUserEntity;

public class PendingCertifiedProductTargetedUserDTO implements Serializable {
    private static final long serialVersionUID = 8450020346312080499L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Long targetedUserId;
    private String name;

    public PendingCertifiedProductTargetedUserDTO() {
    }

    public PendingCertifiedProductTargetedUserDTO(PendingCertifiedProductTargetedUserEntity entity) {
        this.setId(entity.getId());

        if (entity.getMappedProduct() != null) {
            this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
        }
        this.setTargetedUserId(entity.getTargetedUserId());
        this.setName(entity.getName());
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

    public Long getTargetedUserId() {
        return targetedUserId;
    }

    public void setTargetedUserId(final Long targetedUserId) {
        this.targetedUserId = targetedUserId;
    }
}
