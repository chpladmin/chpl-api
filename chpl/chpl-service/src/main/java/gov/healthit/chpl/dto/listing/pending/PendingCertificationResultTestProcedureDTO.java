package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;

public class PendingCertificationResultTestProcedureDTO implements Serializable {
    private static final long serialVersionUID = -3548338679746682621L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long testProcedureId;
    private TestProcedureDTO testProcedure;
    private String enteredName;
    private String version;

    public PendingCertificationResultTestProcedureDTO() {
    }

    public PendingCertificationResultTestProcedureDTO(PendingCertificationResultTestProcedureEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setTestProcedureId(entity.getTestProcedureId());
        if(entity.getTestProcedure() != null) {
            this.testProcedure = new TestProcedureDTO(entity.getTestProcedure());
        }
        this.enteredName = entity.getTestProcedureName();
        this.setVersion(entity.getVersion());
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

    public TestProcedureDTO getTestProcedure() {
        return testProcedure;
    }

    public void setTestProcedure(TestProcedureDTO testProcedure) {
        this.testProcedure = testProcedure;
    }

    public String getEnteredName() {
        return enteredName;
    }

    public void setEnteredName(String enteredName) {
        this.enteredName = enteredName;
    }
}
