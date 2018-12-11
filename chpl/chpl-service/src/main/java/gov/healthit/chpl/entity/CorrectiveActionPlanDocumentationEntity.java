package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "corrective_action_plan_documentation")
public class CorrectiveActionPlanDocumentationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "corrective_action_plan_documentation_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "corrective_action_plan_id", unique = true, nullable = true)
    private CorrectiveActionPlanEntity correctiveActionPlan;

    @Basic(optional = false)
    @Column(name = "filename")
    private String fileName;

    @Column(name = "filetype")
    private String fileType;

    @Basic(optional = false)
    @Column(name = "filedata")
    private byte[] fileData;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @NotNull()
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @NotNull()
    @Column(nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CorrectiveActionPlanEntity getCorrectiveActionPlan() {
        return correctiveActionPlan;
    }

    public void setCorrectiveActionPlan(final CorrectiveActionPlanEntity correctiveActionPlan) {
        this.correctiveActionPlan = correctiveActionPlan;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(final byte[] fileData) {
        this.fileData = fileData;
    }
}
