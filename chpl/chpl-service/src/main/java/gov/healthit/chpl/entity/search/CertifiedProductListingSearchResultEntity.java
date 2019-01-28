package gov.healthit.chpl.entity.search;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.util.Util;

/**
 * Represents one row of one listing's search result data.
 * Will need to be combined with multiple other rows to make a complete listing.
 * @author kekey
 *
 */
@Entity
@Immutable
public class CertifiedProductListingSearchResultEntity {
    private static final long serialVersionUID = -2928445796550377509L;

    @Id
    @Column(name = "unique_id")
    private int uniqueId;

    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUserCount;

    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUsersDate;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

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

    @Column(name = "prev_vendor")
    private String previousDeveloperOwner;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "count_surveillance_activities")
    private Integer countSurveillance;

    @Column(name = "count_open_nonconformities")
    private Integer countOpenNonconformities;

    @Column(name = "count_closed_nonconformities")
    private Integer countClosedNonconformities;

    @Column(name = "cert_number")
    private String cert;

    @Column(name = "cqm_number")
    private String cqm;

    /**
     * Default constructor.
     */
    public CertifiedProductListingSearchResultEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getPreviousDeveloperOwner() {
        return previousDeveloperOwner;
    }

    public void setPreviousDeveloperOwner(final String previousDeveloperOwner) {
        this.previousDeveloperOwner = previousDeveloperOwner;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(final String cert) {
        this.cert = cert;
    }

    public String getCqm() {
        return cqm;
    }

    public void setCqm(final String cqm) {
        this.cqm = cqm;
    }

    public Integer getCountSurveillance() {
        return countSurveillance;
    }

    public void setCountSurveillance(final Integer countSurveillance) {
        this.countSurveillance = countSurveillance;
    }

    public Integer getCountOpenNonconformities() {
        return countOpenNonconformities;
    }

    public void setCountOpenNonconformities(final Integer countOpenNonconformities) {
        this.countOpenNonconformities = countOpenNonconformities;
    }

    public Integer getCountClosedNonconformities() {
        return countClosedNonconformities;
    }

    public void setCountClosedNonconformities(final Integer countClosedNonconformities) {
        this.countClosedNonconformities = countClosedNonconformities;
    }

    public Date getMeaningfulUseUsersDate() {
        return Util.getNewDate(meaningfulUseUsersDate);
    }

    public void setMeaningfulUseUsersDate(Date meaningfulUseUsersDate) {
        this.meaningfulUseUsersDate = Util.getNewDate(meaningfulUseUsersDate);
    }
}
