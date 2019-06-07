package gov.healthit.chpl.domain.search;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Certified Product search result domain object.
 * @author alarned
 *
 */
public class CertifiedProductSearchResult implements Serializable {
    private static final long serialVersionUID = -2547390525592841034L;

    @JsonView({
            SearchViews.Default.class
    })
    private Long id;

    @JsonView({
            SearchViews.Default.class
    })
    private String chplProductNumber;

    @JsonView({
            SearchViews.Default.class
    })
    private String edition;

    @JsonView({
            SearchViews.Default.class
    })
    private String acb;

    @JsonView({
            SearchViews.Default.class
    })
    private String acbCertificationId;

    @JsonView({
            SearchViews.Default.class
    })
    private String practiceType;

    @JsonView({
            SearchViews.Default.class
    })
    private String developer;

    @JsonView({
        SearchViews.Default.class
    })
    private String developerStatus;

    @JsonView({
            SearchViews.Default.class
    })
    private String product;

    @JsonView({
            SearchViews.Default.class
    })
    private String version;

    @JsonView({
            SearchViews.Default.class
    })
    private Long certificationDate;

    @JsonView({
            SearchViews.Default.class
    })
    private String certificationStatus;

    @JsonView({
            SearchViews.Default.class
    })
    private Long surveillanceCount;

    @JsonView({
        SearchViews.Default.class
    })
    private Long openSurveillanceCount;

    @JsonView({
        SearchViews.Default.class
    })
    private Long closedSurveillanceCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long openNonconformityCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long closedNonconformityCount;

    private Long decertificationDate;
    private Long numMeaningfulUse;
    private Long numMeaningfulUseDate;
    private String transparencyAttestationUrl;

    /**
     * Default constructor.
     */
    public CertifiedProductSearchResult() {
    }

    /**
     * Constructed from other search result.
     * @param other the other search result
     */
    public CertifiedProductSearchResult(final CertifiedProductSearchResult other) {
        this.id = other.getId();
        this.chplProductNumber = other.getChplProductNumber();
        this.edition = other.getEdition();
        this.acb = other.getAcb();
        this.acbCertificationId = other.getAcbCertificationId();
        this.practiceType = other.getPracticeType();
        this.developer = other.getDeveloper();
        this.developerStatus = other.getDeveloperStatus();
        this.product = other.getProduct();
        this.version = other.getVersion();
        this.certificationDate = other.getCertificationDate();
        this.certificationStatus = other.getCertificationStatus();
        this.decertificationDate = other.getDecertificationDate();
        this.surveillanceCount = other.getSurveillanceCount();
        this.openNonconformityCount = other.getOpenNonconformityCount();
        this.closedNonconformityCount = other.getClosedNonconformityCount();
        this.numMeaningfulUse = other.getNumMeaningfulUse();
        this.numMeaningfulUseDate = other.getNumMeaningfulUseDate();
        this.transparencyAttestationUrl = other.getTransparencyAttestationUrl();
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

    public String getAcb() {
        return acb;
    }

    public void setAcb(final String acb) {
        this.acb = acb;
    }

    public String getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(final String practiceType) {
        this.practiceType = practiceType;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Long certificationDate) {
        this.certificationDate = certificationDate;
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

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Long getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(final Long decertificationDate) {
        this.decertificationDate = decertificationDate;
    }

    public Long getNumMeaningfulUse() {
        return numMeaningfulUse;
    }

    public void setNumMeaningfulUse(final Long numMeaningfulUse) {
        this.numMeaningfulUse = numMeaningfulUse;
    }

    public Long getNumMeaningfulUseDate() {
        return numMeaningfulUseDate;
    }

    public void setNumMeaningfulUseDate(final Long numMeaningfulUseDate) {
        this.numMeaningfulUseDate = numMeaningfulUseDate;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
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
}
