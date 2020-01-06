package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CertifiedProductSearchBasicDetails implements Serializable {

    private static final long serialVersionUID = 2903219171135034775L;
    private Long id;
    private String chplProductNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEndDate;
    private String acbCertificationId;
    private Map<String, Object> classificationType = new HashMap<String, Object>();
    private String otherAcb;
    private Developer developer;
    private Product product;
    private ProductVersion version;
    private Map<String, Object> certificationEdition = new HashMap<String, Object>();
    private Map<String, Object> practiceType = new HashMap<String, Object>();
    private Map<String, Object> certifyingBody = new HashMap<String, Object>();
    private List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
    private Long certificationDate;
    private Long decertificationDate;
    private Integer countCerts;
    private Integer countCqms;
    private Integer countSurveillance;
    private Integer countOpenSurveillance;
    private Integer countClosedSurveillance;
    private Integer countOpenNonconformities;
    private Integer countClosedNonconformities;
    private InheritedCertificationStatus ics;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private TransparencyAttestation transparencyAttestation;
    private String transparencyAttestationUrl;
    private Long lastModifiedDate;
    private List<Surveillance> surveillance = new ArrayList<Surveillance>();
    private List<CertifiedProductAccessibilityStandard> accessibilityStandards = new ArrayList<CertifiedProductAccessibilityStandard>();
    private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();
    private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();
    private List<CertificationStatusEvent> certificationEvents = new ArrayList<CertificationStatusEvent>();
    private List<MeaningfulUseUser> meaningfulUseUserHistory = new ArrayList<MeaningfulUseUser>();
    private CertifiedProductSed sed;

    @JsonProperty(access = Access.WRITE_ONLY)
    private LegacyCertificationStatus certificationStatus;

    public CertifiedProductSearchBasicDetails() {
        sed = new CertifiedProductSed();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Map<String, Object> getClassificationType() {
        return classificationType;
    }

    public void setClassificationType(Map<String, Object> classificationType) {
        this.classificationType = classificationType;
    }

    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(String otherAcb) {
        this.otherAcb = otherAcb;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public Map<String, Object> getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(Map<String, Object> certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Map<String, Object> getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(Map<String, Object> practiceType) {
        this.practiceType = practiceType;
    }

    public Map<String, Object> getCertifyingBody() {
        return certifyingBody;
    }

    public void setCertifyingBody(Map<String, Object> certifyingBody) {
        this.certifyingBody = certifyingBody;
    }

    public Long getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(Long certificationDate) {
        this.certificationDate = certificationDate;
    }

    public Integer getCountCerts() {
        return countCerts;
    }

    public void setCountCerts(Integer countCertsSuccessful) {
        this.countCerts = countCertsSuccessful;
    }

    public Integer getCountCqms() {
        return countCqms;
    }

    public void setCountCqms(Integer countCQMsSuccessful) {
        this.countCqms = countCQMsSuccessful;
    }

    public List<CertificationStatusEvent> getCertificationEvents() {
        return certificationEvents;
    }

    public void setCertificationEvents(List<CertificationStatusEvent> certificationEvents) {
        this.certificationEvents = certificationEvents;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public TransparencyAttestation getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(TransparencyAttestation transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public InheritedCertificationStatus getIcs() {
        return ics;
    }

    public void setIcs(InheritedCertificationStatus ics) {
        this.ics = ics;
    }

    public List<CertifiedProductTestingLab> getTestingLabs() {
        return testingLabs;
    }

    public void setTestingLabs(List<CertifiedProductTestingLab> testingLabs) {
        this.testingLabs = testingLabs;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public List<CertifiedProductQmsStandard> getQmsStandards() {
        return qmsStandards;
    }

    public void setQmsStandards(List<CertifiedProductQmsStandard> qmsStandards) {
        this.qmsStandards = qmsStandards;
    }

    public List<CertifiedProductTargetedUser> getTargetedUsers() {
        return targetedUsers;
    }

    public void setTargetedUsers(List<CertifiedProductTargetedUser> targetedUsers) {
        this.targetedUsers = targetedUsers;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public List<CertifiedProductAccessibilityStandard> getAccessibilityStandards() {
        return accessibilityStandards;
    }

    public void setAccessibilityStandards(List<CertifiedProductAccessibilityStandard> accessibilityStandards) {
        this.accessibilityStandards = accessibilityStandards;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEndDate() {
        return Util.getNewDate(sedTestingEndDate);
    }

    public void setSedTestingEndDate(Date sedTestingEndDate) {
        this.sedTestingEndDate = Util.getNewDate(sedTestingEndDate);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(ProductVersion version) {
        this.version = version;
    }

    public List<Surveillance> getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(List<Surveillance> surveillance) {
        this.surveillance = surveillance;
    }

    public List<MeaningfulUseUser> getMeaningfulUseUserHistory() {
        return meaningfulUseUserHistory;
    }

    public void setMeaningfulUseUserHistory(List<MeaningfulUseUser> meaningfulUseUserHistory) {
        this.meaningfulUseUserHistory = meaningfulUseUserHistory;
    }

    public Integer getCountSurveillance() {
        return countSurveillance;
    }

    public void setCountSurveillance(Integer countSurveillance) {
        this.countSurveillance = countSurveillance;
    }

    public Integer getCountOpenSurveillance() {
        return countOpenSurveillance;
    }

    public void setCountOpenSurveillance(Integer countOpenSurveillance) {
        this.countOpenSurveillance = countOpenSurveillance;
    }

    public Integer getCountClosedSurveillance() {
        return countClosedSurveillance;
    }

    public void setCountClosedSurveillance(Integer countClosedSurveillance) {
        this.countClosedSurveillance = countClosedSurveillance;
    }

    public Integer getCountOpenNonconformities() {
        return countOpenNonconformities;
    }

    public void setCountOpenNonconformities(Integer countOpenNonconformities) {
        this.countOpenNonconformities = countOpenNonconformities;
    }

    public Integer getCountClosedNonconformities() {
        return countClosedNonconformities;
    }

    public void setCountClosedNonconformities(Integer countClosedNonconformities) {
        this.countClosedNonconformities = countClosedNonconformities;
    }

    public Long getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(Long decertificationDate) {
        this.decertificationDate = decertificationDate;
    }

    public CertifiedProductSed getSed() {
        return sed;
    }

    public void setSed(CertifiedProductSed sed) {
        this.sed = sed;
    }

    /**
     * Retrieve current status.
     *
     * @return current status
     */
    public CertificationStatusEvent getCurrentStatus() {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        CertificationStatusEvent newest = this.getCertificationEvents().get(0);
        for (CertificationStatusEvent event : this.getCertificationEvents()) {
            if (event.getEventDate() > newest.getEventDate()) {
                newest = event;
            }
        }
        return newest;
    }

    /**
     * Retrieve oldest status.
     *
     * @return the first status of the Listing
     */
    public CertificationStatusEvent getOldestStatus() {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        CertificationStatusEvent oldest = this.getCertificationEvents().get(0);
        for (CertificationStatusEvent event : this.getCertificationEvents()) {
            if (event.getEventDate() < oldest.getEventDate()) {
                oldest = event;
            }
        }
        return oldest;
    }

    /**
     * Retrieve certification status on a specific date.
     *
     * @return certification status
     */
    public CertificationStatusEvent getStatusOnDate(Date date) {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        // first we need to make sure the status events are in ascending order
        this.getCertificationEvents().sort(new Comparator<CertificationStatusEvent>() {
            @Override
            public int compare(CertificationStatusEvent o1, CertificationStatusEvent o2) {
                if (o1.getEventDate() == null || o2.getEventDate() == null
                        || o1.getEventDate().equals(o2.getEventDate())) {
                    return 0;
                }
                if (o1.getEventDate() < o2.getEventDate()) {
                    return -1;
                }
                if (o1.getEventDate() > o2.getEventDate()) {
                    return 1;
                }
                return 0;
            }
        });

        CertificationStatusEvent result = null;
        for (int i = 0; i < this.getCertificationEvents().size() && result == null; i++) {
            CertificationStatusEvent currEvent = this.getCertificationEvents().get(i);
            if (i < this.getCertificationEvents().size() - 1) {
                CertificationStatusEvent nextEvent = this.getCertificationEvents().get(i + 1);
                // if the passed-in date is between currEvent and nextEvent then the currEvent
                // gives the status on the passed-in date.
                if (currEvent.getEventDate() != null && currEvent.getEventDate().longValue() <= date
                        .getTime()
                        && nextEvent.getEventDate() != null && nextEvent.getEventDate().longValue() > date
                                .getTime()) {
                    result = currEvent;
                }
            } else {
                result = currEvent;
            }
        }
        return result;
    }

    /**
     * Dynamically determine the current MUU count by finding the most recent MUU entry for this listing.
     */
    public MeaningfulUseUser getCurrentMeaningfulUseUsers() {
        if (this.getMeaningfulUseUserHistory() == null
                || this.getMeaningfulUseUserHistory().size() == 0) {
            return null;
        }

        MeaningfulUseUser newest = this.getMeaningfulUseUserHistory().get(0);
        for (MeaningfulUseUser muuItem : this.getMeaningfulUseUserHistory()) {
            if (muuItem.getMuuDate() > newest.getMuuDate()) {
                newest = muuItem;
            }
        }
        return newest;
    }

    public LegacyCertificationStatus getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(LegacyCertificationStatus certificationStatus) {
        this.certificationStatus = certificationStatus;
    }
}
