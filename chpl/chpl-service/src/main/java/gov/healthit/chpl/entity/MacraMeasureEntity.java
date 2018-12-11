package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "macra_criteria_map")
public class MacraMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "value")
    private String value;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    protected Date creationDate;

    @Column(nullable = false)
    protected Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    protected Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public CertificationCriterionEntity getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(final CertificationCriterionEntity certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
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
