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
@Table(name = "corrective_action_plan_certification_result")
public class CorrectiveActionPlanCertificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "corrective_action_plan_certification_result_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "corrective_action_plan_id", unique = true, nullable = true)
    private CorrectiveActionPlanEntity correctiveActionPlan;

    @Basic(optional = false)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "summary")
    private String summary;

    @Column(name = "developer_explanation")
    private String developerExplanation;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "num_sites_passed")
    private Integer numSitesPassed;

    @Column(name = "num_sites_total")
    private Integer numSitesTotal;

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

    public CertificationCriterionEntity getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(final CertificationCriterionEntity certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getDeveloperExplanation() {
        return developerExplanation;
    }

    public void setDeveloperExplanation(final String developerExplanation) {
        this.developerExplanation = developerExplanation;
    }

    public Integer getNumSitesPassed() {
        return numSitesPassed;
    }

    public void setNumSitesPassed(final Integer numSitesPassed) {
        this.numSitesPassed = numSitesPassed;
    }

    public Integer getNumSitesTotal() {
        return numSitesTotal;
    }

    public void setNumSitesTotal(final Integer numSitesTotal) {
        this.numSitesTotal = numSitesTotal;
    }
}
