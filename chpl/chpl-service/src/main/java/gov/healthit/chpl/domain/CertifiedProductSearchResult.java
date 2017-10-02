package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CertifiedProductSearchResult implements Serializable {
    private static final long serialVersionUID = 5076651267693735935L;
    private Long id;
    private Long testingLabId;
    private String testingLabName;
    private String chplProductNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
    private String acbCertificationId;
    private Map<String, Object> classificationType = new HashMap<String, Object>();
    private String otherAcb;
    private Map<String, Object> certificationStatus = new HashMap<String, Object>();
    private Map<String, Object> developer = new HashMap<String, Object>();
    private Map<String, Object> product = new HashMap<String, Object>();
    private Map<String, Object> certificationEdition = new HashMap<String, Object>();
    private Map<String, Object> practiceType = new HashMap<String, Object>();
    private Map<String, Object> certifyingBody = new HashMap<String, Object>();
    private Long certificationDate;
    private Long decertificationDate;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private String transparencyAttestation;
    private String transparencyAttestationUrl;
    private Integer countCerts;
    private Integer countCqms;
    private Integer countSurveillance;
    private Integer countOpenSurveillance;
    private Integer countClosedSurveillance;
    private Integer countOpenNonconformities;
    private Integer countClosedNonconformities;

    private Long numMeaningfulUse;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTestingLabId() {
        return testingLabId;
    }

    public void setTestingLabId(final Long testingLabId) {
        this.testingLabId = testingLabId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Map<String, Object> getClassificationType() {
        return classificationType;
    }

    public void setClassificationType(final Map<String, Object> classificationType) {
        this.classificationType = classificationType;
    }

    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(final String otherAcb) {
        this.otherAcb = otherAcb;
    }

    public Map<String, Object> getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Map<String, Object> developer) {
        this.developer = developer;
    }

    public Map<String, Object> getProduct() {
        return product;
    }

    public void setProduct(final Map<String, Object> product) {
        this.product = product;
    }

    public Map<String, Object> getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final Map<String, Object> certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Map<String, Object> getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(final Map<String, Object> practiceType) {
        this.practiceType = practiceType;
    }

    public Map<String, Object> getCertifyingBody() {
        return certifyingBody;
    }

    public void setCertifyingBody(final Map<String, Object> certifyingBody) {
        this.certifyingBody = certifyingBody;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Long certificationDate) {
        this.certificationDate = certificationDate;
    }

    public Integer getCountCerts() {
        return countCerts;
    }

    public void setCountCerts(final Integer countCerts) {
        this.countCerts = countCerts;
    }

    public Integer getCountCqms() {
        return countCqms;
    }

    public void setCountCqms(final Integer countCqms) {
        this.countCqms = countCqms;
    }

    public Map<String, Object> getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final Map<String, Object> certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final String transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public String getTestingLabName() {
        return testingLabName;
    }

    public void setTestingLabName(final String testingLabName) {
        this.testingLabName = testingLabName;
    }

    public Boolean getIcs() {
        return ics;
    }

    public void setIcs(final Boolean ics) {
        this.ics = ics;
    }

    public Boolean getSedTesting() {
        return sedTesting;
    }

    public void setSedTesting(final Boolean sedTesting) {
        this.sedTesting = sedTesting;
    }

    public Boolean getQmsTesting() {
        return qmsTesting;
    }

    public void setQmsTesting(final Boolean qmsTesting) {
        this.qmsTesting = qmsTesting;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(final String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(final Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(final String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEnd() {
        return sedTestingEnd;
    }

    public void setSedTestingEnd(final Date sedTestingEnd) {
        this.sedTestingEnd = sedTestingEnd;
    }

    public Long getNumMeaningfulUse() {
        return numMeaningfulUse;
    }

    public void setNumMeaningfulUse(final Long numMeaningfulUse) {
        this.numMeaningfulUse = numMeaningfulUse;
    }

    public Integer getCountSurveillance() {
        return countSurveillance;
    }

    public void setCountSurveillance(final Integer countSurveillance) {
        this.countSurveillance = countSurveillance;
    }

    public Integer getCountOpenSurveillance() {
        return countOpenSurveillance;
    }

    public void setCountOpenSurveillance(final Integer countOpenSurveillance) {
        this.countOpenSurveillance = countOpenSurveillance;
    }

    public Integer getCountClosedSurveillance() {
        return countClosedSurveillance;
    }

    public void setCountClosedSurveillance(final Integer countClosedSurveillance) {
        this.countClosedSurveillance = countClosedSurveillance;
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

    public Long getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(final Long decertificationDate) {
        this.decertificationDate = decertificationDate;
    }

}
