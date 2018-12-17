package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.util.Util;

public class CQMCriterionDTO implements Serializable {
    private static final long serialVersionUID = -2794095309532708038L;
    private String cmsId;
    private Long cqmCriterionTypeId;
    private String cqmDomain;
    private Long cqmVersionId;
    private String cqmVersion;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Long id;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String nqfNumber;
    private String number;
    private String title;
    private Boolean retired;

    public CQMCriterionDTO() {
    }

    public CQMCriterionDTO(CQMCriterionEntity entity) {
        this();
        this.cmsId = entity.getCmsId();
        this.cqmCriterionTypeId = entity.getCqmCriterionTypeId();
        this.cqmDomain = entity.getCqmDomain();
        this.cqmVersionId = entity.getCqmVersionId();
        this.cqmVersion = entity.getCqmVersion();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.description = entity.getDescription();
        this.id = entity.getId();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.nqfNumber = entity.getNqfNumber();
        this.number = entity.getNumber();
        this.title = entity.getTitle();
        this.setRetired(entity.getRetired());

    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(final String cmsId) {
        this.cmsId = cmsId;
    }

    public Long getCqmCriterionTypeId() {
        return cqmCriterionTypeId;
    }

    public void setCqmCriterionTypeId(final Long cqmCriterionTypeId) {
        this.cqmCriterionTypeId = cqmCriterionTypeId;
    }

    public String getCqmDomain() {
        return cqmDomain;
    }

    public void setCqmDomain(final String cqmDomain) {
        this.cqmDomain = cqmDomain;
    }

    public Long getCqmVersionId() {
        return cqmVersionId;
    }

    public void setCqmVersionId(final Long cqmVersion) {
        this.cqmVersionId = cqmVersion;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(final String nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getCqmVersion() {
        return cqmVersion;
    }

    public void setCqmVersion(final String cqmVersion) {
        this.cqmVersion = cqmVersion;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(final Boolean retired) {
        this.retired = retired;
    }
}
