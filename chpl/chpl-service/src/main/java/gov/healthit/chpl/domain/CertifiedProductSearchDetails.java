package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CertifiedProductSearchDetails {
	
	private Long id;
    private String chplProductNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String acbCertificationId;
    private Map<String, Object> classificationType = new HashMap<String, Object>();
    private String otherAcb;
    private Map<String, Object> certificationStatus = new HashMap<String, Object>();
	private Map<String, Object> developer = new HashMap<String, Object>();
	private Map<String, Object> product = new HashMap<String, Object>();
	private Map<String, Object> certificationEdition = new HashMap<String, Object>();
	private Map<String, Object> practiceType = new HashMap<String, Object>();
	private Map<String, Object> certifyingBody = new HashMap<String, Object>();
	private Map<String, Object> testingLab = new HashMap<String, Object>();
	private Long certificationDate;
	private Integer countCerts;
	private Integer countCqms;
	private Integer countCorrectiveActionPlans;
	private Integer countCurrentCorrectiveActionPlans;
	private Integer countClosedCorrectiveActionPlans;
	private Boolean visibleOnChpl;
	private String termsOfUse;
	private String apiDocumentation;
	private Boolean ics;
	private String productAdditionalSoftware;
	private String transparencyAttestation;
	private String transparencyAttestationUrl;
	private Long lastModifiedDate;
	
	private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();
	private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();
	private List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
	private List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
	private List<CertificationEvent> certificationEvents = new ArrayList<CertificationEvent>();
	
	private Set<String> warningMessages = new HashSet<String>();
	private Set<String> errorMessages = new HashSet<String>();
	
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
	public Map<String, Object> getDeveloper() {
		return developer;
	}
	public void setDeveloper(Map<String, Object> developer) {
		this.developer = developer;
	}
	public Map<String, Object> getProduct() {
		return product;
	}
	public void setProduct(Map<String, Object> product) {
		this.product = product;
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
	public Boolean getVisibleOnChpl() {
		return visibleOnChpl;
	}
	public void setVisibleOnChpl(Boolean visibleOnChpl) {
		this.visibleOnChpl = visibleOnChpl;
	}
	public List<CertificationEvent> getCertificationEvents(){
		return certificationEvents;
	}
	public void setCertificationEvents(
			List<CertificationEvent> certificationEvents) {
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
	public Integer getCountCorrectiveActionPlans() {
		return countCorrectiveActionPlans;
	}
	public void setCountCorrectiveActionPlans(Integer countCorrectiveActionPlans) {
		this.countCorrectiveActionPlans = countCorrectiveActionPlans;
	}
	public String getTermsOfUse() {
		return termsOfUse;
	}
	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}
	public String getApiDocumentation() {
		return apiDocumentation;
	}
	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}
	public String getTransparencyAttestation() {
		return transparencyAttestation;
	}
	public void setTransparencyAttestation(String transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
	public Boolean getIcs() {
		return ics;
	}
	public void setIcs(Boolean ics) {
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
	public Integer getCountCurrentCorrectiveActionPlans() {
		return countCurrentCorrectiveActionPlans;
	}
	public void setCountCurrentCorrectiveActionPlans(Integer countCurrentCorrectiveActionPlans) {
		this.countCurrentCorrectiveActionPlans = countCurrentCorrectiveActionPlans;
	}
	public Integer getCountClosedCorrectiveActionPlans() {
		return countClosedCorrectiveActionPlans;
	}
	public void setCountClosedCorrectiveActionPlans(Integer countClosedCorrectiveActionPlans) {
		this.countClosedCorrectiveActionPlans = countClosedCorrectiveActionPlans;
	}
	public List<CertifiedProductTargetedUser> getTargetedUsers() {
		return targetedUsers;
	}
	public void setTargetedUsers(List<CertifiedProductTargetedUser> targetedUsers) {
		this.targetedUsers = targetedUsers;
	}
}
