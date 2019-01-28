package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.MacraMeasureEntity;
import gov.healthit.chpl.util.Util;

public class MacraMeasureDTO implements Serializable {
    private static final long serialVersionUID = -1863384989196377585L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterionDTO criteria;
    private String value;
    private String name;
    private String description;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public MacraMeasureDTO() {
    }

    public MacraMeasureDTO(MacraMeasureEntity entity) {
        this();

        this.id = entity.getId();
        this.criteriaId = entity.getCertificationCriterionId();
        if (entity.getCertificationCriterion() != null) {
            this.criteria = new CertificationCriterionDTO(entity.getCertificationCriterion());
        }
        this.value = entity.getValue();
        this.name = entity.getName();
        this.description = entity.getDescription();

        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(final Long criteriaId) {
        this.criteriaId = criteriaId;
    }

    public CertificationCriterionDTO getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterionDTO criteria) {
        this.criteria = criteria;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
