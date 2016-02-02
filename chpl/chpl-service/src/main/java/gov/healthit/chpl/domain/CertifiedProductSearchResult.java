package gov.healthit.chpl.domain;


import java.util.HashMap;
import java.util.Map;


public class CertifiedProductSearchResult {
	
	
	private Long id;
    private Long testingLabId;
    private String testingLabName;
    private String chplProductNumber;
    private String reportFileLocation;
    private String qualityManagementSystemAtt;
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
	private Boolean privacyAttestation;
	private String termsOfUse;
	private String apiDocumentation;
	private String ics;
	private Boolean sedTesting;
	private Boolean qmsTesting;
	private Boolean transparencyAttestation;
	private Integer countCerts;
	private Integer countCqms;
	private Integer countCorrectiveActionPlans;
	
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
	public String getQualityManagementSystemAtt() {
		return qualityManagementSystemAtt;
	}
	public void setQualityManagementSystemAtt(String qualityManagementSystemAtt) {
		this.qualityManagementSystemAtt = qualityManagementSystemAtt;
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
	public Boolean getPrivacyAttestation() {
		return privacyAttestation;
	}
	public void setPrivacyAttestation(Boolean privacyAttestation) {
		this.privacyAttestation = privacyAttestation;
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
	public Boolean getTransparencyAttestation() {
		return transparencyAttestation;
	}
	public void setTransparencyAttestation(Boolean transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
	public String getTestingLabName() {
		return testingLabName;
	}
	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}
	public String getIcs() {
		return ics;
	}
	public void setIcs(String ics) {
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
	
}
