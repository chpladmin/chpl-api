package gov.healthit.chpl.entity.search;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;

/**
 * Entity used for basic search results.
 * @author alarned
 *
 */
@Entity
@Table(name = "certified_product_search")
public class CertifiedProductBasicSearchResultEntity {
    private static final long serialVersionUID = -2928065796550377869L;

    @Id
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "year")
    private String edition;

    @Column(name = "certification_body_name")
    private String acbName;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "product_version")
    private String version;

    @Column(name = "product_name")
    private String product;

    @Column(name = "vendor_name")
    private String developer;

    @Column(name = "vendor_status_name")
    private String developerStatus;

    @Column(name = "owner_history")
    private String previousDevelopers;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "api_documentation")
    private String apiDocumentation;

    @Column(name = "surveillance_count")
    private Long surveillanceCount;

    @Column(name = "open_surveillance_count")
    private Long openSurveillanceCount;

    @Column(name = "closed_surveillance_count")
    private Long closedSurveillanceCount;

    @Column(name = "open_nonconformity_count")
    private Long openNonconformityCount;

    @Column(name = "closed_nonconformity_count")
    private Long closedNonconformityCount;

    @Column(name = "surv_dates")
    private String survDates;

    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUserCount;

    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUserDate;

    public Date getMeaningfulUseUserDate() {
        return Util.getNewDate(meaningfulUseUserDate);
    }

    public void setMeaningfulUseUserDate(Date meaningfulUseUserDate) {
        this.meaningfulUseUserDate = Util.getNewDate(meaningfulUseUserDate);
    }

    @Column(name = "certs")
    private String certs; // comma-separated list of all certification criteria
    // met by the certified product

    @Column(name = "cqms")
    private String cqms; // comma-separated list of all cqms met by the
    // certified product

    @Column(name = "parent")
    private String parent; // comma-separated list of all parents

    @Column(name = "child")
    private String child; // comma-separated list of all children

    /**
     * Default constructor.
     */
    public CertifiedProductBasicSearchResultEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getChild() {
        return child;
    }

    public void setChild(final String child) {
        this.child = child;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(final String edition) {
        this.edition = edition;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
    }

    public String getPracticeTypeName() {
        return practiceTypeName;
    }

    public void setPracticeTypeName(final String practiceTypeName) {
        this.practiceTypeName = practiceTypeName;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(final String developer) {
        this.developer = developer;
    }

    public String getDeveloperStatus() {
        return developerStatus;
    }

    public void setDeveloperStatus(final String developerStatus) {
        this.developerStatus = developerStatus;
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public Long getSurveillanceCount() {
        return surveillanceCount;
    }

    public void setSurveillanceCount(final Long surveillanceCount) {
        this.surveillanceCount = surveillanceCount;
    }

    public Long getOpenNonconformityCount() {
        return openNonconformityCount;
    }

    public void setOpenNonconformityCount(final Long openNonconformityCount) {
        this.openNonconformityCount = openNonconformityCount;
    }

    public Long getClosedNonconformityCount() {
        return closedNonconformityCount;
    }

    public void setClosedNonconformityCount(final Long closedNonconformityCount) {
        this.closedNonconformityCount = closedNonconformityCount;
    }

    public String getCerts() {
        return certs;
    }

    public void setCerts(final String certs) {
        this.certs = certs;
    }

    public String getCqms() {
        return cqms;
    }

    public void setCqms(final String cqms) {
        this.cqms = cqms;
    }

    public String getPreviousDevelopers() {
        return previousDevelopers;
    }

    public void setPreviousDevelopers(final String previousDevelopers) {
        this.previousDevelopers = previousDevelopers;
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Long getMeaningfulUseUserCount() {
        return meaningfulUseUserCount;
    }

    public void setMeaningfulUseUserCount(final Long meaningfulUseUserCount) {
        this.meaningfulUseUserCount = meaningfulUseUserCount;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }

    public Long getOpenSurveillanceCount() {
        return openSurveillanceCount;
    }

    public void setOpenSurveillanceCount(final Long openSurveillanceCount) {
        this.openSurveillanceCount = openSurveillanceCount;
    }

    public Long getClosedSurveillanceCount() {
        return closedSurveillanceCount;
    }

    public void setClosedSurveillanceCount(final Long closedSurveillanceCount) {
        this.closedSurveillanceCount = closedSurveillanceCount;
    }

    public String getSurvDates() {
        return survDates;
    }

    public void setSurvDates(final String survDates) {
        this.survDates = survDates;
    }
}
