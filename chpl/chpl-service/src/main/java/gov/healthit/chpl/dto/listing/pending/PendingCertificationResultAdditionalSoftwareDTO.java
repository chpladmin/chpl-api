package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;

public class PendingCertificationResultAdditionalSoftwareDTO implements Serializable {
    private static final long serialVersionUID = 13724871794367054L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long certifiedProductId;
    private String chplId;
    private String name;
    private String version;
    private String justification;
    private String grouping;

    public PendingCertificationResultAdditionalSoftwareDTO() {
    }

    public PendingCertificationResultAdditionalSoftwareDTO(PendingCertificationResultAdditionalSoftwareEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setCertifiedProductId(entity.getCertifiedProductId());
        this.setChplId(entity.getChplId());
        this.setName(entity.getSoftwareName());
        this.setVersion(entity.getSoftwareVersion());
        this.setJustification(entity.getJustification());
        this.setGrouping(entity.getGrouping());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertificationResultId() {
        return pendingCertificationResultId;
    }

    public void setPendingCertificationResultId(final Long pendingCertificationResultId) {
        this.pendingCertificationResultId = pendingCertificationResultId;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public String getChplId() {
        return chplId;
    }

    public void setChplId(final String chplId) {
        this.chplId = chplId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(final String grouping) {
        this.grouping = grouping;
    }
}
