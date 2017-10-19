package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;

public class PendingCertificationResultTestProcedureDTO implements Serializable {
    private static final long serialVersionUID = -3548338679746682621L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long testProcedureId;
    private String version;

    public PendingCertificationResultTestProcedureDTO() {
    }

    public PendingCertificationResultTestProcedureDTO(PendingCertificationResultTestProcedureEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setTestProcedureId(entity.getTestProcedureId());
        this.setVersion(entity.getTestProcedureVersion());
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

    public Long getTestProcedureId() {
        return testProcedureId;
    }

    public void setTestProcedureId(final Long testProcedureId) {
        this.testProcedureId = testProcedureId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
