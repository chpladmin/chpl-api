package gov.healthit.chpl.entity.surveillance;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_surveillance_requirement")
public class PendingSurveillanceRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pending_surveillance_id")
    private Long pendingSurveillanceId;

    @Column(name = "type_value")
    private String requirementType;

    @Column(name = "requirement")
    private String surveilledRequirement;

    @Column(name = "result_value")
    private String result;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceRequirementId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_requirement_id", nullable = false)
    private Set<PendingSurveillanceNonconformityEntity> nonconformities = new HashSet<PendingSurveillanceNonconformityEntity>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingSurveillanceId() {
        return pendingSurveillanceId;
    }

    public void setPendingSurveillanceId(final Long pendingSurveillanceId) {
        this.pendingSurveillanceId = pendingSurveillanceId;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(final String requirementType) {
        this.requirementType = requirementType;
    }

    public String getSurveilledRequirement() {
        return surveilledRequirement;
    }

    public void setSurveilledRequirement(final String surveilledRequirement) {
        this.surveilledRequirement = surveilledRequirement;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
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

    public Set<PendingSurveillanceNonconformityEntity> getNonconformities() {
        return nonconformities;
    }

    public void setNonconformities(final Set<PendingSurveillanceNonconformityEntity> nonconformities) {
        this.nonconformities = nonconformities;
    }
}
