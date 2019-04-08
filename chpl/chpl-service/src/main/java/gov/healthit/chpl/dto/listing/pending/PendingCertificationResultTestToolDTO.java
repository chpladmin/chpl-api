package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestToolEntity;

public class PendingCertificationResultTestToolDTO implements Serializable {
    private static final long serialVersionUID = -1021740511347040742L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long testToolId;
    private String name;
    private String version;

    public PendingCertificationResultTestToolDTO() {
    }

    public PendingCertificationResultTestToolDTO(PendingCertificationResultTestToolEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setTestToolId(entity.getTestToolId());
        this.setName(entity.getTestToolName());
        this.setVersion(entity.getTestToolVersion());
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getTestToolId() {
        return testToolId;
    }

    public void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
