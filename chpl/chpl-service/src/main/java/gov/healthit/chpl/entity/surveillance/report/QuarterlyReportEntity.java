package gov.healthit.chpl.entity.surveillance.report;

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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "quarterly_report")
public class QuarterlyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "annual_report_id")
    private Long annualReportId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_report_id", insertable = false, updatable = false)
    private AnnualReportEntity annualReport;

    @Column(name = "quarter_id")
    private Long quarterId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", insertable = false, updatable = false)
    private QuarterEntity quarter;

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

    public Long getAnnualReportId() {
        return annualReportId;
    }

    public void setAnnualReportId(final Long annualReportId) {
        this.annualReportId = annualReportId;
    }

    public AnnualReportEntity getAnnualReport() {
        return annualReport;
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
