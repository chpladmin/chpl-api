package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertifiedProductSearchDetails {
	
	private Long id;
    private Long testingLabId;
    private String chplProductNumber;
    private String reportFileLocation;
    private String qualityManagementSystemAtt;
    private String acbCertificationId;
    private Map<String, String> classificationType = new HashMap<String, String>();
    private String otherAcb;
    private String certificationStatusId;
	private Map<String, String> vendor = new HashMap<String, String>();
	private Map<String, String> product = new HashMap<String, String>();
	private Map<String, String> certificationEdition = new HashMap<String, String>();
	private Map<String, String> practiceType = new HashMap<String, String>();
	private Map<String, String> certifyingBody = new HashMap<String, String>();
	private String certificationDate;
	private Integer countCerts;
	private Integer countCqms;
	private Boolean visibleOnChpl;
	private List<AdditionalSoftware> additionalSoftware = new ArrayList<AdditionalSoftware>();
	private List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
	private List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
	private List<CQMCriterion> applicableCqmCriteria = new ArrayList<CQMCriterion>();
	
	
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
	public Map<String, String> getClassificationType() {
		return classificationType;
	}
	public void setClassificationType(Map<String, String> classificationType) {
		this.classificationType = classificationType;
	}
	public String getOtherAcb() {
		return otherAcb;
	}
	public void setOtherAcb(String otherAcb) {
		this.otherAcb = otherAcb;
	}
	public String getCertificationStatusId() {
		return certificationStatusId;
	}
	public void setCertificationStatusId(String certificationStatusId) {
		this.certificationStatusId = certificationStatusId;
	}
	public Map<String, String> getVendor() {
		return vendor;
	}
	public void setVendor(Map<String, String> vendor) {
		this.vendor = vendor;
	}
	public Map<String, String> getProduct() {
		return product;
	}
	public void setProduct(Map<String, String> product) {
		this.product = product;
	}
	public Map<String, String> getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(Map<String, String> certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
	public Map<String, String> getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(Map<String, String> practiceType) {
		this.practiceType = practiceType;
	}
	public Map<String, String> getCertifyingBody() {
		return certifyingBody;
	}
	public void setCertifyingBody(Map<String, String> certifyingBody) {
		this.certifyingBody = certifyingBody;
	}
	//public List<String> getAdditionalSoftware() {
	//	return additionalSoftware;
	//}
	//public void setAdditionalSoftware(List<String> additionalSoftware) {
	//	this.additionalSoftware = additionalSoftware;
	//}
	public String getCertificationDate() {
		return certificationDate;
	}
	public void setCertificationDate(String certificationDate) {
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
	public List<AdditionalSoftware> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<AdditionalSoftware> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public List<CQMCriterion> getApplicableCqmCriteria() {
		return applicableCqmCriteria;
	}
	public void setApplicableCqmCriteria(List<CQMCriterion> applicableCqmCriteria) {
		this.applicableCqmCriteria = applicableCqmCriteria;
	}
	public Boolean getVisibleOnChpl() {
		return visibleOnChpl;
	}
	public void setVisibleOnChpl(Boolean visibleOnChpl) {
		this.visibleOnChpl = visibleOnChpl;
	}
	
}
