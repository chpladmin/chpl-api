package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Certified Product Search Details entity.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertifiedProductSearchDetails implements Serializable {

    private static final long serialVersionUID = 2903219171127034775L;
    public static final String ACB_ID_KEY = "id";
    public static final String ACB_NAME_KEY = "name";
    public static final String EDITION_ID_KEY = "id";
    public static final String EDITION_NAME_KEY = "name";

    /**
     * The internal ID of the certified product.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The unique CHPL ID of the certified product. This variable is applicable to 2014 and 2015 Edition. New uploads to
     * CHPL will use the format: CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date
     */
    @XmlElement(required = true)
    private String chplProductNumber;

    /**
     * A hyperlink to the test results used to certify the Complete EHRs and/or EHR Modules that can be accessed by the
     * public. This variable is applicable to 2014 Edition. Fully qualified URL which is reachable via web browser
     * validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String reportFileLocation;

    /**
     * Hyperlink to FULL Usability Test Report meeting all the SED requirements. This variable is applicable for 2014
     * and 2015 Edition. Fully qualified URL which is reachable via web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String sedReportFileLocation;

    /**
     * For SED testing, a description of the intended users of the Health IT
     */
    @XmlElement(required = false, nillable = true)
    private String sedIntendedUserDescription;

    /**
     * Date all SED testing was concluded for the Health IT. The format for the date is YYYMMDD
     */
    @XmlElement(required = false, nillable = true)
    private Date sedTestingEndDate;

    /**
     * The ID used by ONC-ACBs for internal tracking for 2014 and 2015 Certification Edition. It is a string variable
     * that does not have any restrictions on formatting or values.
     */
    @XmlElement(required = true)
    private String acbCertificationId;

    /**
     * The classification of the certified product (either complete or modular). It is only applicable to 2014 Edition,
     * and takes values of either Complete EHR or Modular EHR.
     */
    @XmlElement(required = false, nillable = true)
    private Map<String, Object> classificationType = new HashMap<String, Object>();

    /**
     * If there was previously a different certifying body managing this listing this is their name.
     */
    @XmlElement(required = false, nillable = true)
    private String otherAcb;

    /**
     * The developer or vendor of the certified health IT product listing.
     */
    @XmlElement(required = true)
    private Developer developer;

    /**
     * The product which this listing is under.
     */
    @XmlElement(required = true)
    private Product product;

    /**
     * The version of the product being uploaded. This variable is applicable for 2014 and 2015 Edition.
     */
    @XmlElement(required = true)
    private ProductVersion version;

    /**
     * The certification edition. It takes a value of 2011, 2014 or 2015.
     */
    @XmlElement(required = true)
    private Map<String, Object> certificationEdition = new HashMap<String, Object>();

    /**
     * For 2014 products, the practice setting for which the certified product is designed. It takes value of Ambulatory
     * or Inpatient.
     */
    @XmlElement(required = false, nillable = true)
    private Map<String, Object> practiceType = new HashMap<String, Object>();

    /**
     * The ONC-ACB responsible for certifying the Health IT Module. This variable is applicable to 2014 and 2015
     * Edition, and allowable values are: Drummond Group, ICSA Labs, UL LLC.
     */
    @XmlElement(required = true)
    private Map<String, Object> certifyingBody = new HashMap<String, Object>();

    /**
     * The ATL responsible for testing the Health IT Module. It is applicable for 2014 and 2015 Edition and takes values
     * of: Drummond Group, ICSA Labs, UL LLC, National Technical Systems, SLI Global, CCHIT
     */
    @XmlElementWrapper(name = "testingLabs", nillable = true, required = false)
    @XmlElement(name = "testingLab")
    @Singular
    private List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();

    /**
     * Certification date represented in milliseconds since epoch
     */
    @XmlElement(required = true)
    private Long certificationDate;

    /**
     * Decertification date represented in milliseconds since epoch
     */
    @XmlElement(required = false, nillable = true)
    private Long decertificationDate;

    /**
     * Number of certification criteria this listing attests to.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countCerts;
    /**
     * Number of cqms this listing attests to.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countCqms;

    /**
     * Total count of open+closed surveillance for this listing.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countSurveillance;

    /**
     * Total count of open surveillance for this listing.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countOpenSurveillance;

    /**
     * Total count of closed surveillance for this listing.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countClosedSurveillance;

    /**
     * The total number of open (unresolved) non-conformities found for the corresponding listing. For additional
     * information, please see 'Understanding Surveillance Information in the CHPL', available in the CHPL Public User
     * Guide
     */
    @XmlElement(required = false, nillable = true)
    private Integer countOpenNonconformities;

    /**
     * Total count of closed nonconformities for this listing.
     */
    @XmlElement(required = false, nillable = true)
    private Integer countClosedNonconformities;

    /**
     * This variable indicates whether or not the certification issued was a result of an inherited certified status
     * request. This variable is applicable for 2014 and 2015 Edition and contains the inherited status as well as
     * first-level parents and children.
     */
    @XmlElement(required = false, nillable = true)
    private InheritedCertificationStatus ics;

    /**
     * This variable identifies if Health IT Module was certified to the accessibility-centered design certification
     * criterion for 2015 Edition. It is a binary variable that takes value of true or false.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean accessibilityCertified;

    /**
     * For legacy CHPL listings, any additional software needed.
     */
    @XmlElement(required = false, nillable = true)
    private String productAdditionalSoftware;

    /**
     * The transparency attestation required by 170.523(k)(2). It is applicable for 2014 and 2015 Edition and takes
     * value of Affirmative, Negative, or N/A.
     */
    @XmlElement(name = "transparencyAttestation", required = false, nillable = true)
    private TransparencyAttestation transparencyAttestation;

    /**
     * A hyperlink to the mandatory disclosures required by 170.523(k)(1) for the Health IT Module
     */
    @XmlElement(required = false, nillable = true)
    private String transparencyAttestationUrl;

    /**
     * The last time this listing was modified in any way given in milliseconds since epoch.
     */
    @XmlElement(required = true)
    private Long lastModifiedDate;

    /**
     * Any surveillance that has occurred on this listing
     */
    @XmlElementWrapper(name = "surveillanceList", nillable = true, required = false)
    @XmlElement(name = "surveillance")
    private List<Surveillance> surveillance = new ArrayList<Surveillance>();

    /**
     * This variable indicates that if there is the standard(s) or lack thereof used to meet the accessibility-centered
     * design certification criterion for 2015 Certification Edtion. It is a string variable that does not have any
     * restrictions on formatting or values.
     */
    @XmlElementWrapper(name = "accessibilityStandards", nillable = true, required = false)
    @XmlElement(name = "accessibilityStandard")
    @Singular
    @SuppressWarnings({"checkstyle:linelength"})
    private List<CertifiedProductAccessibilityStandard> accessibilityStandards = new ArrayList<CertifiedProductAccessibilityStandard>();

    /**
     * Description of the health IT module(s) intended users for the tested capabilities/related criteria. This variable
     * is applicable only for 2015 Edition, and a string variable that does not take any restrictions on formatting or
     * values.
     */
    @XmlElementWrapper(name = "targetedUsers", nillable = true, required = false)
    @XmlElement(name = "targetedUser")
    @Singular
    private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();

    /**
     * The standard or mapping used to meet the quality management system certification criterion. This variable is
     * applicable for 2014 and 2015 Edition, and a string variable that does not take any restrictions on formatting or
     * values.
     */
    @XmlElementWrapper(name = "qmsStandards", nillable = true, required = false)
    @XmlElement(name = "qmsStandard")
    @Singular
    private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();

    /**
     * This variable indicates if the listing was successfully tested for automated
     * numerator recording. It is applicable for the 2015 edition. The allowable values include
     * measures such as "EP", "EH/CAH", "EP Individual", "EC Individual (TIN/NPI)", "EC Group", etc.
     */
    @XmlElementWrapper(name = "g1MacraMeasures", nillable = true, required = false)
    @XmlElement(name = "macraMeasure")
    @Singular
    private List<MacraMeasure> g1MacraMeasures = new ArrayList<MacraMeasure>();

    /**
     * This variable indicates if the listing was successfully tested for automated measure
     * calculation. It is applicable for the 2015 edition. The allowable values include
     * measures such as "EP", "EH/CAH", "EP Individual", "EC Individual (TIN/NPI)", "EC Group", etc.
     */
    @XmlElementWrapper(name = "g2MacraMeasures", nillable = true, required = false)
    @XmlElement(name = "macraMeasure")
    @Singular
    private List<MacraMeasure> g2MacraMeasures = new ArrayList<MacraMeasure>();

    /**
     * The criteria to which this listing attests
     */
    @XmlElementWrapper(name = "certificationResults", nillable = true, required = false)
    @XmlElement(name = "certificationResult")
    @Singular
    private List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();

    /**
     * The clinical quality measures to which this listing has been certified.
     */
    @XmlElementWrapper(name = "cqmResults", nillable = true, required = false)
    @XmlElement(name = "cqmResult")
    private List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();

    /**
     * This property exists solely to be able to deserialize listing activity events from very old data. Since we care
     * about certification status changes when categorizing listing activity we need to be able to read this value in
     * old listing activity event data. Not all old listing properties need to be present for this reason. This property
     * should not be visible in the generated XSD or any response from an API call.
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    @XmlTransient
    private LegacyCertificationStatus certificationStatus;

    /**
     * All current and historical certification status of this listing. The certification statuses take values of
     * Active; Suspended by ONC; Suspended by ONC-ACB; Withdrawn by Developer; Withdrawn by Developer Under
     * Surveillance/Review; Withdrawn by ONC-ACB; Terminated by ONC; Retired. For a detailed description of each
     * certification status, please see 'Understanding Certification Status in the CHPL', available in the CHPL Public
     * User Guide.
     */
    @XmlElementWrapper(name = "certificationEvents", nillable = true, required = false)
    @XmlElement(name = "certificationEvent")
    @Singular
    private List<CertificationStatusEvent> certificationEvents = new ArrayList<CertificationStatusEvent>();

    /**
     * This variable identifies if the listing meets the definition of "Cures Update".
     * It is a binary variable that takes value of true or false.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean curesUpdate;

    /**
     * All current and historical values of meaningful use users for this listing along with the dates each meaningful
     * use user count was valid. Dates are given in milliseconds since epoch.
     */
    @XmlElementWrapper(name = "meaningfulUseUserHistory", nillable = true, required = false)
    @XmlElement(name = "meaningfulUseEntry")
    private List<MeaningfulUseUser> meaningfulUseUserHistory = new ArrayList<MeaningfulUseUser>();

    /**
     * All data related to safety-enhanced design for this listing.
     */
    @XmlElement(name = "sed", nillable = true, required = false)
    private CertifiedProductSed sed = new CertifiedProductSed();

    /**
     * URL where the listing's Real World Testing Plan is located
     */
    @XmlElement(name = "rwt_plans_url", nillable = true, required = false)
    private String rwtPlansUrl;

    /**
     * Date the listing's Real World Testing Plan was submitted
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(name = "rwt_plans_check_date", nillable = true, required = false)
    private LocalDate rwtPlansCheckDate;

    /**
     * URL where the listing's Real World Testing Results is located
     */
    @XmlElement(name = "rwt_results_url", nillable = true, required = false)
    private String rwtResultsUrl;

    /**
     * Date the listing's Real World Testing Results was submitted
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(name = "rwt_results_check_date", nillable = true, required = false)
    private LocalDate rwtResultsCheckDate;

    /**
     * First year that the listing is eligible for Real World Testing data
     */
    @XmlElement(name = "rwt_eligibility_year", nillable = true, required = false)
    private Integer rwtEligibilityYear;

    @Builder.Default
    @XmlTransient
    private Set<String> warningMessages = new HashSet<String>();

    @Builder.Default
    @XmlTransient
    private Set<String> errorMessages = new HashSet<String>();

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

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
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

    public List<CertificationResult> getCertificationResults() {
        return certificationResults;
    }

    public void setCertificationResults(final List<CertificationResult> certificationResults) {
        this.certificationResults = certificationResults;
    }

    public List<CQMResultDetails> getCqmResults() {
        return cqmResults;
    }

    public void setCqmResults(final List<CQMResultDetails> cqmResults) {
        this.cqmResults = cqmResults;
    }

    public Integer getCountCerts() {
        return countCerts;
    }

    public void setCountCerts(final Integer countCertsSuccessful) {
        this.countCerts = countCertsSuccessful;
    }

    public Integer getCountCqms() {
        return countCqms;
    }

    public void setCountCqms(final Integer countCQMsSuccessful) {
        this.countCqms = countCQMsSuccessful;
    }

    public List<CertificationStatusEvent> getCertificationEvents() {
        return certificationEvents;
    }

    public void setCertificationEvents(final List<CertificationStatusEvent> certificationEvents) {
        this.certificationEvents = certificationEvents;
    }

    public void setCuresUpdate(Boolean curesUpdate) {
        this.curesUpdate = curesUpdate;
    }

    public Boolean getCuresUpdate() {
        return curesUpdate;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(final Set<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(final Set<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public TransparencyAttestation getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final TransparencyAttestation transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public InheritedCertificationStatus getIcs() {
        return ics;
    }

    public void setIcs(final InheritedCertificationStatus ics) {
        this.ics = ics;
    }

    public List<CertifiedProductTestingLab> getTestingLabs() {
        return testingLabs;
    }

    public void setTestingLabs(final List<CertifiedProductTestingLab> testingLabs) {
        this.testingLabs = testingLabs;
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

    public List<CertifiedProductQmsStandard> getQmsStandards() {
        return qmsStandards;
    }

    public void setQmsStandards(final List<CertifiedProductQmsStandard> qmsStandards) {
        this.qmsStandards = qmsStandards;
    }

    public List<MacraMeasure> getG1MacraMeasures() {
        return this.g1MacraMeasures;
    }

    public void setG1MacraMeasures(List<MacraMeasure> g1MacraMeasures) {
        this.g1MacraMeasures = g1MacraMeasures;
    }

    public List<MacraMeasure> getG2MacraMeasures() {
        return this.g2MacraMeasures;
    }

    public void setG2MacraMeasures(List<MacraMeasure> g2MacraMeasures) {
        this.g2MacraMeasures = g2MacraMeasures;
    }

    public List<CertifiedProductTargetedUser> getTargetedUsers() {
        return targetedUsers;
    }

    public void setTargetedUsers(final List<CertifiedProductTargetedUser> targetedUsers) {
        this.targetedUsers = targetedUsers;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(final Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public List<CertifiedProductAccessibilityStandard> getAccessibilityStandards() {
        return accessibilityStandards;
    }

    public void setAccessibilityStandards(
            final List<CertifiedProductAccessibilityStandard> accessibilityStandards) {
        this.accessibilityStandards = accessibilityStandards;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(final String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEndDate() {
        return Util.getNewDate(sedTestingEndDate);
    }

    public void setSedTestingEndDate(final Date sedTestingEndDate) {
        this.sedTestingEndDate = Util.getNewDate(sedTestingEndDate);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }

    public ProductVersion getVersion() {
        return version;
    }

    public void setVersion(final ProductVersion version) {
        this.version = version;
    }

    public List<Surveillance> getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(final List<Surveillance> surveillance) {
        this.surveillance = surveillance;
    }

    public List<MeaningfulUseUser> getMeaningfulUseUserHistory() {
        return meaningfulUseUserHistory;
    }

    public void setMeaningfulUseUserHistory(final List<MeaningfulUseUser> meaningfulUseUserHistory) {
        this.meaningfulUseUserHistory = meaningfulUseUserHistory;
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

    public CertifiedProductSed getSed() {
        return sed;
    }

    public void setSed(final CertifiedProductSed sed) {
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
    public CertificationStatusEvent getStatusOnDate(final Date date) {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        // first we need to make sure the status events are in ascending order
        this.getCertificationEvents().sort(new Comparator<CertificationStatusEvent>() {
            @Override
            public int compare(final CertificationStatusEvent o1, final CertificationStatusEvent o2) {
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
                if (currEvent.getEventDate() != null && currEvent.getEventDate().longValue() <= date.getTime()
                        && nextEvent.getEventDate() != null && nextEvent.getEventDate().longValue() > date.getTime()) {
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

    public String getRwtPlansUrl() {
        return rwtPlansUrl;
    }

    public void setRwtPlansUrl(String rwtPlansUrl) {
        this.rwtPlansUrl = rwtPlansUrl;
    }

    public LocalDate getRwtPlansCheckDate() {
        return rwtPlansCheckDate;
    }

    public void setRwtPlansCheckDate(LocalDate rwtPlansCheckDate) {
        this.rwtPlansCheckDate = rwtPlansCheckDate;
    }

    public String getRwtResultsUrl() {
        return rwtResultsUrl;
    }

    public void setRwtResultsUrl(String rwtResultsUrl) {
        this.rwtResultsUrl = rwtResultsUrl;
    }

    public LocalDate getRwtResultsCheckDate() {
        return rwtResultsCheckDate;
    }

    public void setRwtResultsCheckDate(LocalDate rwtResultsCheckDate) {
        this.rwtResultsCheckDate = rwtResultsCheckDate;
    }

    public Integer getRwtEligibilityYear() {
        return rwtEligibilityYear;
    }

    public void setRwtEligibilityYear(Integer rwtEligibilityYear) {
        this.rwtEligibilityYear = rwtEligibilityYear;
    }
}
