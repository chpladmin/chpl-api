package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestToolEntity;

public class CertificationResultTestToolDTO implements Serializable {
    private static final long serialVersionUID = -3977375618330009861L;
    private Long id;
    private Long certificationResultId;
    private Long testToolId;
    private String testToolName;
    private String testToolVersion;
    private boolean retired;
    private Boolean deleted;

    public CertificationResultTestToolDTO() {
    }

    public CertificationResultTestToolDTO(CertificationResultTestToolEntity entity) {
        this.id = entity.getId();
        this.certificationResultId = entity.getCertificationResultId();
        this.testToolId = entity.getTestToolId();
        this.testToolVersion = entity.getVersion();
        if (entity.getTestTool() != null) {
            this.testToolName = entity.getTestTool().getName();
            this.retired = entity.getTestTool().getRetired();
        }
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationResultId() {
        return certificationResultId;
    }

    public void setCertificationResultId(final Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getTestToolId() {
        return testToolId;
    }

    public void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    public String getTestToolName() {
        return testToolName;
    }

    public void setTestToolName(final String testToolName) {
        this.testToolName = testToolName;
    }

    public String getTestToolVersion() {
        return testToolVersion;
    }

    public void setTestToolVersion(final String testToolVersion) {
        this.testToolVersion = testToolVersion;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(final boolean retired) {
        this.retired = retired;
    }

}
