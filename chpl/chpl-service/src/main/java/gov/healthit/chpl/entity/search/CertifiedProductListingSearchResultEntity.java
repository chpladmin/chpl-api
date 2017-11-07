package gov.healthit.chpl.entity.search;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents one listing's worth of search result data.
 * Currently only used with the API search.
 * @author kekey
 *
 */
@Entity
@Table(name = "certified_product_search_result")
public class CertifiedProductListingSearchResultEntity {
    private static final long serialVersionUID = -2928445796550377509L;

    @Id
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_name")
    private String certificationStatus;
    
    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUserCount;
    
    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;
    
    @Column(name = "year")
    private String edition;

    @Column(name = "testing_lab_name")
    private String atlName;

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

    @Column(name = "owner_history")
    private Set<String> previousDeveloperOwners;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "cert_number")
    private Set<String> certs;

    @Column(name = "cqm_number")
    private Set<String> cqms;

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

    public String getAtlName() {
        return atlName;
    }

    public void setAtlName(final String atlName) {
        this.atlName = atlName;
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
        return certificationDate;
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = certificationDate;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public Date getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = decertificationDate;
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

    public Set<String> getPreviousDeveloperOwners() {
        return previousDeveloperOwners;
    }

    public void setPreviousDeveloperOwners(Set<String> previousDeveloperOwners) {
        this.previousDeveloperOwners = previousDeveloperOwners;
    }

    public Set<String> getCerts() {
        return certs;
    }

    public void setCerts(Set<String> certs) {
        this.certs = certs;
    }

    public Set<String> getCqms() {
        return cqms;
    }

    public void setCqms(Set<String> cqms) {
        this.cqms = cqms;
    }
}
