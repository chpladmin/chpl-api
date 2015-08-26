package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertifiedProductSearchDetails {
	
	private Long id;
	private Map<String, String> vendor = new HashMap<String, String>();
	private Map<String, String> product = new HashMap<String, String>();
	private String version;
	private String chplNum;
	private String practiceType;
	private String classification;
	private Map<String, String> certifyingBody = new HashMap<String, String>();
	private String certificationEdition;
	private Date certDate;
	private Date lastModifiedDate;
	private List<String> additionalSoftware = new ArrayList<String>();
	private ModificationItem lastModifiedItem;
	private String certsAndCQMs;
	private List<CertificationResult> certs;
	private List<CQMResult> cqms;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCertsAndCQMs() {
		return certsAndCQMs;
	}
	public void setCertsAndCQMs(String certsAndCQMs) {
		this.certsAndCQMs = certsAndCQMs;
	}
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public Map<String,String> getCertifyingBody() {
		return certifyingBody;
	}
	public void setCertifyingBody(Map<String, String> certifyingBody) {
		this.certifyingBody = certifyingBody;
	}
	public String getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
	public List<String> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<String> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public Date getCertDate() {
		return certDate;
	}
	public void setCertDate(Date certDate) {
		this.certDate = certDate;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public ModificationItem getLastModifiedItem() {
		return lastModifiedItem;
	}
	public void setLastModifiedItem(ModificationItem lastModifiedItem) {
		this.lastModifiedItem = lastModifiedItem;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}
	public List<CertificationResult> getCerts() {
		return certs;
	}
	public void setCerts(List<CertificationResult> certs) {
		this.certs = certs;
	}
	public List<CQMResult> getCqms() {
		return cqms;
	}
	public void setCqms(List<CQMResult> cqms) {
		this.cqms = cqms;
	}
	public String getChplNum() {
		return chplNum;
	}
	public void setChplNum(String chplNum) {
		this.chplNum = chplNum;
	}
	
	
	
}
