package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.TestToolEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "certification_result_test_tool")
public class CertificationResultTestToolEntity implements Serializable {
    private static final long serialVersionUID = -4001381872451124331L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_result_test_tool_id")
    private Long id;

    @Basic(optional = true)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "test_tool_id")
    private Long testToolId;

    @Column(name = "version")
    private String version;

    @Basic(optional = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_tool_id", unique = true, nullable = true, insertable = false, updatable = false)
    private TestToolEntity testTool;

    @Basic(optional = true)
    @ManyToOne(targetEntity = CertificationResultEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_result_id", nullable = false, insertable = false, updatable = false)
    private CertificationResultEntity certificationResult;

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

    public Long getTestToolId() {
        return testToolId;
    }

    public void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    public TestToolEntity getTestTool() {
        return testTool;
    }

    public void setTestTool(final TestToolEntity testTool) {
        this.testTool = testTool;
    }

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    protected Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    protected Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    protected Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public CertificationResultEntity getCertificationResult() {
        return certificationResult;
    }

    public void setCertificationResult(final CertificationResultEntity certificationResult) {
        this.certificationResult = certificationResult;
    }
}
