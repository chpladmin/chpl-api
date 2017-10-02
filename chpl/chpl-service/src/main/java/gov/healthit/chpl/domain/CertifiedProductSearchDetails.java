package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertifiedProductSearchDetails implements Serializable {
	private static final long serialVersionUID = 2903219171127034775L;

	/**
	 * The internal ID of the certified product.
	 */
	@XmlElement(required = true)
	private Long id;

	/**
	 * The unique CHPL ID of the certified product.  This variable is applicable to 2014
	 * and 2015 Edition. New uploads to CHPL will use the format:
	 * CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date
	 */
	@XmlElement(required = true)
    private String chplProductNumber;

	/**
	 * A hyperlink to the test results used to certify the Complete EHRs and/or EHR Modules that
	 * can be accessed by the public. This variable is applicable to 2014 Edition.
	 * Fully qualified URL which is reachable via web browser validation and verification.
	 */
	@XmlElement(required = false, nillable = true)
    private String reportFileLocation;

	/**
	 * Hyperlink to FULL Usability Test Report meeting all the SED requirements. This variable is
	 * applicable for 2014 and 2015 Edition. Fully qualified URL which is reachable via web
	 * browser validation and verification.
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
    private Date sedTestingEnd;

	/**
	 * The ID used by ONC-ACBs for internal tracking for 2014 and 2015 Certification Edition.
	 * It is a string variable that does not have any restrictions on formatting or values.
	 */
	@XmlElement(required = true)
    private String acbCertificationId;

	/**
	 * The classification of the certified product (either complete or modular).
	 * It is only applicable to 2014 Edition, and takes values of either Complete EHR or Modular EHR.
	 */
	@XmlElement(required = false, nillable = true)
    private Map<String, Object> classificationType = new HashMap<String, Object>();

	/**
	 * If there was previously a different certifying body managing this listing this is their name.
	 */
	@XmlElement(required = false, nillable = true)
    private String otherAcb;

	/**
	 * The certification status for the corresponding listing. This variable takes value of
	 * Active; Suspended by ONC; Suspended by ONC-ACB; Withdrawn by Developer;
	 * Withdrawn by Developer Under Surveillance/Review; Withdrawn by ONC-ACB; Terminated by ONC;
	 * Retired. For a detailed description of each certification status, please see
	 * 'Understanding Certification Status in the CHPL', available in the CHPL Public User Guide.
	 */
	@XmlElement(required = true)
    private Map<String, Object> certificationStatus = new HashMap<String, Object>();

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
	 * The version of the product being uploaded.
	 * This variable is applicable for 2014 and 2015 Edition.
	 */
	@XmlElement(required = true)
    private ProductVersion version;

	/**
	 * The certification edition. It takes a value of 2011, 2014 or 2015.
	 */
	@XmlElement(required = true)
	private Map<String, Object> certificationEdition = new HashMap<String, Object>();

	/**
	 * For 2014 products, the practice setting for which the certified product is designed.
	 * It takes value of Ambulatory or Inpatient.
	 */
	@XmlElement(required = false, nillable = true)
	private Map<String, Object> practiceType = new HashMap<String, Object>();

	/**
	 * The ONC-ACB responsible for certifying the Health IT Module. This variable is applicable
	 * to 2014 and 2015 Edition, and allowable values are: Drummond Group, ICSA Labs, Infogard.
	 */
	@XmlElement(required = true)
	private Map<String, Object> certifyingBody = new HashMap<String, Object>();

	/**
	 * The ATL responsible for testing the Health IT Module. It is applicable for 2014 and
	 * 2015 Edition and takes values of: Drummond Group, ICSA Labs, Infogard,
	 * National Technical Systems, SLI Global, CCHIT
	 */
	@XmlElement(required = false, nillable = true)
	private Map<String, Object> testingLab = new HashMap<String, Object>();

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
	 * The total number of open (unresolved) non-conformities found for the corresponding listing.
	 * For additional information, please see 'Understanding Surveillance Information
	 * in the CHPL', available in the CHPL Public User Guide
	 */
	@XmlElement(required = false, nillable = true)
	private Integer countOpenNonconformities;

	/**
	 * Total count of closed nonconformities for this listing.
	 */
	@XmlElement(required = false, nillable = true)
	private Integer countClosedNonconformities;

	/**
	 * This variable indicates whether or not the certification issued was a result
	 * of an inherited certified status request. This variable is applicable for 2014 and
	 * 2015 Edition and contains the inherited status as well as first-level parents and
	 * children.
	 */
	@XmlElement(required = false, nillable = true)
	private InheritedCertificationStatus ics;

	/**
	 * This variable identifies if  Health IT Module was certified to the
	 * accessibility-centered design certification criterion for 2015 Edition.
	 * It is a binary variable that takes value of true or false.
	 */
	@XmlElement(required = false, nillable = true)
	private Boolean accessibilityCertified;

	/**
	 * For legacy CHPL listings, any additional software needed.
	 */
	@XmlElement(required = false, nillable = true)
	private String productAdditionalSoftware;

	/**
	 * The transparency attestation required by 170.523(k)(2). It is applicable for
	 * 2014 and 2015 Edition and takes value of Affirmative, Negative, or N/A.
	 */
	@XmlElement(required = false, nillable = true)
	private String transparencyAttestation;

	/**
	 * A hyperlink to the mandatory disclosures required by 170.523(k)(1) for the Health IT Module
	 */
	@XmlElement(required = false, nillable = true)
	private String transparencyAttestationUrl;

	/**
	 * Number of meaningful use users for this listing as uploaded by the certifying body.
	 */
	@XmlElement(required = false, nillable = true)
	private Long numMeaningfulUse;

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
	 * This variable indicates that if  there is the standard(s) or lack thereof used to meet the accessibility-centered design certification criterion for 2015 Certification Edtion.
	 * It is a string variable that does not have any restrictions on formatting or values.
	 */
	@XmlElementWrapper(name = "accessibilityStandards", nillable = true, required = false)
	@XmlElement(name = "accessibilityStandard")
	private List<CertifiedProductAccessibilityStandard> accessibilityStandards = new ArrayList<CertifiedProductAccessibilityStandard>();

	/**
	 * Description of the health IT module(s) intended users for the tested
	 * capabilities/related criteria.  This variable is applicable only for 2015 Edition,
	 * and a string variable that does not take any restrictions on formatting or values.
	 *
	 */
	@XmlElementWrapper(name = "targetedUsers", nillable = true, required = false)
	@XmlElement(name = "targetedUser")
	private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();

	/**
	 * The standard or mapping used to meet the quality management system
	 * certification criterion. This variable is applicable for 2014 and 2015 Edition,
	 * and a string variable that does not take any restrictions on formatting or values.
	 */
	@XmlElementWrapper(name = "qmsStandards", nillable = true, required = false)
	@XmlElement(name = "qmsStandard")
	private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();

	/**
	 * The criteria to which this listing attests
	 */
	@XmlElementWrapper(name = "certificationResults", nillable = true, required = false)
	@XmlElement(name = "certificationResult")
	private List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();

	/**
	 * The clinical quality measures to which this listing has been certified.
	 */
	@XmlElementWrapper(name = "cqmResults", nillable = true, required = false)
	@XmlElement(name = "cqmResult")
	private List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();

	/**
	 * Changes to the certification status of this listing
	 */
	@XmlElementWrapper(name = "certificationEvents", nillable = true, required = false)
	@XmlElement(name = "certificationEvent")
	private List<CertificationStatusEvent> certificationEvents = new ArrayList<CertificationStatusEvent>();

	/**
	 * All data related to safety-enhanced design for this listing.
	 */
	@XmlElement(name = "sed", nillable = true, required = false)
	private CertifiedProductSed sed;

	@XmlTransient
	private Set<String> warningMessages = new HashSet<String>();

	@XmlTransient
	private Set<String> errorMessages = new HashSet<String>();

	public CertifiedProductSearchDetails() {
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
	public List<CertificationResult> getCertificationResults() {
		return certificationResults;
	}
	public void setCertificationResults(
			List<CertificationResult> certificationResults) {
		this.certificationResults = certificationResults;
	}
	public List<CQMResultDetails> getCqmResults() {
		return cqmResults;
	}
	public void setCqmResults(List<CQMResultDetails> cqmResults) {
		this.cqmResults = cqmResults;
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
	public void setCertificationEvents(
			List<CertificationStatusEvent> certificationEvents) {
		this.certificationEvents = certificationEvents;
	}
	public Long getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Map<String, Object> getCertificationStatus() {
		return certificationStatus;
	}
	public void setCertificationStatus(Map<String, Object> certificationStatus) {
		this.certificationStatus = certificationStatus;
	}
	public Set<String> getWarningMessages() {
		return warningMessages;
	}
	public void setWarningMessages(Set<String> warningMessages) {
		this.warningMessages = warningMessages;
	}
	public Set<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(Set<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public String getTransparencyAttestation() {
		return transparencyAttestation;
	}
	public void setTransparencyAttestation(String transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
	public InheritedCertificationStatus getIcs() {
		return ics;
	}
	public void setIcs(InheritedCertificationStatus ics) {
		this.ics = ics;
	}

	public Map<String, Object> getTestingLab() {
		return testingLab;
	}
	public void setTestingLab(Map<String, Object> testingLab) {
		this.testingLab = testingLab;
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
	public Date getSedTestingEnd() {
		return sedTestingEnd;
	}
	public void setSedTestingEnd(Date sedTestingEnd) {
		this.sedTestingEnd = sedTestingEnd;
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
	public Long getNumMeaningfulUse() {
		return numMeaningfulUse;
	}
	public void setNumMeaningfulUse(Long numMeaningfulUse) {
		this.numMeaningfulUse = numMeaningfulUse;
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
}
