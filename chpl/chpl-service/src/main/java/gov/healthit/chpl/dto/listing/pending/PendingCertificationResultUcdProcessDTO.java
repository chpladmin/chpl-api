package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;

public class PendingCertificationResultUcdProcessDTO implements Serializable {
    private static final long serialVersionUID = -2422389792105890975L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long ucdProcessId;
    private String ucdProcessName;
    private String ucdProcessDetails;

    public PendingCertificationResultUcdProcessDTO() {
    }

    public PendingCertificationResultUcdProcessDTO(PendingCertificationResultUcdProcessEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setUcdProcessName(entity.getUcdProcessName());
        this.setUcdProcessId(entity.getUcdProcessId());
        this.setUcdProcessDetails(entity.getUcdProcessDetails());
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

    public Long getUcdProcessId() {
        return ucdProcessId;
    }

    public void setUcdProcessId(final Long ucdProcessId) {
        this.ucdProcessId = ucdProcessId;
    }

    public String getUcdProcessDetails() {
        return ucdProcessDetails;
    }

    public void setUcdProcessDetails(final String ucdProcessDetails) {
        this.ucdProcessDetails = ucdProcessDetails;
    }

    public String getUcdProcessName() {
        return ucdProcessName;
    }

    public void setUcdProcessName(final String ucdProcessName) {
        this.ucdProcessName = ucdProcessName;
    }
}
