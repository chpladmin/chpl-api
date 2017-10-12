package gov.healthit.chpl.domain.search;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

public class CertifiedProductSearchResult implements Serializable {
    private static final long serialVersionUID = -2547390525592841034L;

    @JsonView({
            SearchViews.Default.class
    })
    protected Long id;

    @JsonView({
            SearchViews.Default.class
    })
    protected String chplProductNumber;

    @JsonView({
            SearchViews.Default.class
    })
    protected String edition;

    @JsonView({
            SearchViews.Default.class
    })
    protected String atl;

    @JsonView({
            SearchViews.Default.class
    })
    protected String acb;

    @JsonView({
            SearchViews.Default.class
    })
    protected String acbCertificationId;

    @JsonView({
            SearchViews.Default.class
    })
    protected String practiceType;

    @JsonView({
            SearchViews.Default.class
    })
    protected String developer;

    @JsonView({
            SearchViews.Default.class
    })
    protected String product;

    @JsonView({
            SearchViews.Default.class
    })
    protected String version;

    @JsonView({
            SearchViews.Default.class
    })
    protected Long certificationDate;

    @JsonView({
            SearchViews.Default.class
    })
    protected String certificationStatus;

    @JsonView({
            SearchViews.Default.class
    })
    protected Long surveillanceCount;

    @JsonView({
            SearchViews.Default.class
    })
    protected Long openNonconformityCount;

    @JsonView({
            SearchViews.Default.class
    })
    protected Long closedNonconformityCount;

    protected Long decertificationDate;
    protected Long numMeaningfulUse;
    protected String transparencyAttestationUrl;

    public CertifiedProductSearchResult() {
    }

    public CertifiedProductSearchResult(CertifiedProductSearchResult other) {
        this.id = other.getId();
        this.chplProductNumber = other.getChplProductNumber();
        this.edition = other.getEdition();
        this.atl = other.getAtl();
        this.acb = other.getAcb();
        this.acbCertificationId = other.getAcbCertificationId();
        this.practiceType = other.getPracticeType();
        this.developer = other.getDeveloper();
        this.product = other.getProduct();
        this.version = other.getVersion();
        this.certificationDate = other.getCertificationDate();
        this.certificationStatus = other.getCertificationStatus();
        this.decertificationDate = other.getDecertificationDate();
        this.surveillanceCount = other.getSurveillanceCount();
        this.openNonconformityCount = other.getOpenNonconformityCount();
        this.closedNonconformityCount = other.getClosedNonconformityCount();
        this.numMeaningfulUse = other.getNumMeaningfulUse();
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

    public String getAtl() {
        return atl;
    }

    public void setAtl(final String atl) {
        this.atl = atl;
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

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }
}
