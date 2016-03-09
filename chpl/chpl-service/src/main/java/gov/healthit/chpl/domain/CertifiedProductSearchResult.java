package gov.healthit.chpl.domain;


import java.util.HashMap;
import java.util.Map;


public class CertifiedProductSearchResult {
	private Long id;
    private Long testingLabId;
    private String testingLabName;
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
	private Long certificationDate;
	private Boolean visibleOnChpl;
	private String termsOfUse;
	private String apiDocumentation;
	private Boolean ics;
	private Boolean sedTesting;
	private Boolean qmsTesting;
	private String productAdditionalSoftware;
	private String transparencyAttestation;
	private String transparencyAttestationUrl;
	private Integer countCerts;
	private Integer countCqms;
	private Integer countCorrectiveActionPlans;
	private Integer countCurrentCorrectiveActionPlans;
	private Integer countClosedCorrectiveActionPlans;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTestingLabId() {
		return testingLabId;
	}
	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
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
	public Integer getCountCerts() {
		return countCerts;
	}
	public void setCountCerts(Integer countCerts) {
		this.countCerts = countCerts;
	}
	public Integer getCountCqms() {
		return countCqms;
	}
	public void setCountCqms(Integer countCqms) {
		this.countCqms = countCqms;
	}
	public Boolean getVisibleOnChpl() {
		return visibleOnChpl;
	}
	public void setVisibleOnChpl(Boolean visibleOnChpl) {
		this.visibleOnChpl = visibleOnChpl;
	}
	public Map<String, Object> getCertificationStatus() {
		return certificationStatus;
	}
	public void setCertificationStatus(Map<String, Object> certificationStatus) {
		this.certificationStatus = certificationStatus;
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
	public String getTestingLabName() {
		return testingLabName;
	}
	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}
	public Boolean getIcs() {
		return ics;
	}
	public void setIcs(Boolean ics) {
		this.ics = ics;
	}
	public Boolean getSedTesting() {
		return sedTesting;
	}
	public void setSedTesting(Boolean sedTesting) {
		this.sedTesting = sedTesting;
	}
	public Boolean getQmsTesting() {
		return qmsTesting;
	}
	public void setQmsTesting(Boolean qmsTesting) {
		this.qmsTesting = qmsTesting;
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
	
}
