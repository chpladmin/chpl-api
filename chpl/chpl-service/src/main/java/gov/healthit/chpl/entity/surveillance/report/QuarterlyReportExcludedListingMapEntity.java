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

import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;

@Entity
@Table(name = "quarterly_report_excluded_listing_map")
public class QuarterlyReportExcludedListingMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quarterly_report_id")
    private Long quarterlyReportId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarterly_report_id", insertable = false, updatable = false)
    private QuarterlyReportEntity quarterlyReport;

    @Column(name = "listing_id")
    private Long listingId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity listing;

    @Column(name = "reason")
    private String reason;

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

    public Long getQuarterlyReportId() {
        return quarterlyReportId;
    }

    public void setQuarterlyReportId(final Long quarterlyReportId) {
        this.quarterlyReportId = quarterlyReportId;
    }

    public QuarterlyReportEntity getQuarterlyReport() {
        return quarterlyReport;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(final Long listingId) {
        this.listingId = listingId;
    }

    public CertifiedProductDetailsEntity getListing() {
        return listing;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
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
