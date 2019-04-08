package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductQmsStandardEntity;

public class PendingCertifiedProductQmsStandardDTO implements Serializable {
    private static final long serialVersionUID = 6800290521633648570L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Long qmsStandardId;
    private String name;
    private String modification;
    private String applicableCriteria;

    public PendingCertifiedProductQmsStandardDTO() {
    }

    public PendingCertifiedProductQmsStandardDTO(PendingCertifiedProductQmsStandardEntity entity) {
        this.setId(entity.getId());

        if (entity.getMappedProduct() != null) {
            this.setPendingCertifiedProductId(entity.getMappedProduct().getId());
        }
        this.setQmsStandardId(entity.getQmsStandardId());
        this.setName(entity.getName());
        this.setModification(entity.getModification());
        this.setApplicableCriteria(entity.getApplicableCriteria());
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

    public Long getQmsStandardId() {
        return qmsStandardId;
    }

    public void setQmsStandardId(final Long qmsStandardId) {
        this.qmsStandardId = qmsStandardId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(final String modification) {
        this.modification = modification;
    }

    public String getApplicableCriteria() {
        return applicableCriteria;
    }

    public void setApplicableCriteria(final String applicableCriteria) {
        this.applicableCriteria = applicableCriteria;
    }
}
