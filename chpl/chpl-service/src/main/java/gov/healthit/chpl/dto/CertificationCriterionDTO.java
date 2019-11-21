package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.util.Util;

public class CertificationCriterionDTO implements Serializable {
    private static final long serialVersionUID = -1129602624256345286L;
    private Long id;
    private Boolean automatedMeasureCapable;
    private Boolean automatedNumeratorCapable;
    private Long certificationEditionId;
    private String certificationEdition;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String number;
    private Boolean requiresSed;
    private String title;
    private Boolean removed;

    public CertificationCriterionDTO() {
    }

    public CertificationCriterionDTO(final CertificationCriterionEntity entity) {

        this.id = entity.getId();
        this.automatedMeasureCapable = entity.isAutomatedMeasureCapable();
        this.automatedNumeratorCapable = entity.isAutomatedNumeratorCapable();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.certificationEdition = entity.getCertificationEdition().getYear();
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.description = entity.getDescription();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.number = entity.getNumber();

        this.requiresSed = entity.isRequiresSed();
        this.title = entity.getTitle();
        this.removed = entity.getRemoved();
    }

    public CertificationCriterionDTO(final CertificationCriterion domain) {

        this.id = domain.getId();
        this.certificationEditionId = domain.getCertificationEditionId();
        if (domain.getCertificationEdition() != null) {
            this.certificationEdition = domain.getCertificationEdition();
        }
        this.description = domain.getDescription();
        this.number = domain.getNumber();
        this.title = domain.getTitle();
        this.removed = domain.getRemoved();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Boolean getAutomatedMeasureCapable() {
        return automatedMeasureCapable;
    }

    public void setAutomatedMeasureCapable(final Boolean automatedMeasureCapable) {
        this.automatedMeasureCapable = automatedMeasureCapable;
    }

    public Boolean getAutomatedNumeratorCapable() {
        return automatedNumeratorCapable;
    }

    public void setAutomatedNumeratorCapable(final Boolean automatedNumeratorCapable) {
        this.automatedNumeratorCapable = automatedNumeratorCapable;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public Boolean getRequiresSed() {
        return requiresSed;
    }

    public void setRequiresSed(final Boolean requiresSed) {
        this.requiresSed = requiresSed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final String certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(final Boolean removed) {
        this.removed = removed;
    }
}
