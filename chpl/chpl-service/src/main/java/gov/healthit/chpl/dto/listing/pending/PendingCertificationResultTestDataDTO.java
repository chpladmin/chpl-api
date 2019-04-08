package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;

public class PendingCertificationResultTestDataDTO implements Serializable {
    private static final long serialVersionUID = -6051286661261913772L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long testDataId;
    private TestDataDTO testData;
    private String enteredName;
    private String version;
    private String alteration;

    public PendingCertificationResultTestDataDTO() {
    }

    public PendingCertificationResultTestDataDTO(PendingCertificationResultTestDataEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setTestDataId(entity.getTestDataId());
        if(entity.getTestData() != null) {
            this.setTestData(new TestDataDTO(entity.getTestData()));
        }
        this.enteredName = entity.getTestDataName();
        this.setVersion(entity.getVersion());
        this.setAlteration(entity.getAlteration());
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

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(final String alteration) {
        this.alteration = alteration;
    }

    public Long getTestDataId() {
        return testDataId;
    }

    public void setTestDataId(Long testDataId) {
        this.testDataId = testDataId;
    }

    public TestDataDTO getTestData() {
        return testData;
    }

    public void setTestData(TestDataDTO testData) {
        this.testData = testData;
    }

    public String getEnteredName() {
        return enteredName;
    }

    public void setEnteredName(String enteredName) {
        this.enteredName = enteredName;
    }

}
