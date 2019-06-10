package gov.healthit.chpl.entity.surveillance.report;

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

import gov.healthit.chpl.entity.CertificationBodyEntity;

@Entity
@Table(name = "quarterly_report")
public class QuarterlyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = false, updatable = false)
    private CertificationBodyEntity acb;

    @Column(name = "year")
    private Integer year;

    @Column(name = "quarter_id")
    private Long quarterId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", insertable = false, updatable = false)
    private QuarterEntity quarter;

    @Column(name = "activities_and_outcomes_summary")
    private String activitiesOutcomesSummary;

    @Column(name = "reactive_summary")
    private String reactiveSummary;

    @Column(name = "prioritized_element_summary")
    private String prioritizedElementSummary;

    @Column(name = "transparency_disclosure_summary")
    private String transparencyDisclosureSummary;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public CertificationBodyEntity getAcb() {
        return acb;
    }

    public Long getQuarterId() {
        return quarterId;
    }

    public void setQuarterId(final Long quarterId) {
        this.quarterId = quarterId;
    }

    public QuarterEntity getQuarter() {
        return quarter;
    }

    public String getActivitiesOutcomesSummary() {
        return activitiesOutcomesSummary;
    }

    public void setActivitiesOutcomesSummary(final String activitiesOutcomesSummary) {
        this.activitiesOutcomesSummary = activitiesOutcomesSummary;
    }

    public String getReactiveSummary() {
        return reactiveSummary;
    }

    public void setReactiveSummary(final String reactiveSummary) {
        this.reactiveSummary = reactiveSummary;
    }

    public String getPrioritizedElementSummary() {
        return prioritizedElementSummary;
    }

    public void setPrioritizedElementSummary(final String prioritizedElementSummary) {
        this.prioritizedElementSummary = prioritizedElementSummary;
    }

    public String getTransparencyDisclosureSummary() {
        return transparencyDisclosureSummary;
    }

    public void setTransparencyDisclosureSummary(final String transparencyDisclosureSummary) {
        this.transparencyDisclosureSummary = transparencyDisclosureSummary;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
