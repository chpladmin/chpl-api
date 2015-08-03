package gov.healthit.chpl.domain;

import java.util.Date;
import java.util.List;

public class CertifiedProductSearchDetails {
	
	private Long id;
	private String chplNum;
	private String vendor;
	private String product;
	private String version;
	private String certsAndCQMs;
	private String classification;
	private String certifyingBody;
	private String certificationEdition;
	private String practiceType;
	private String edition;
	private Date certDate;
	private Date lastModifiedDate;
	private List<String> additionalSoftware;
	private ModificationItem lastModifiedItem;
	private List<CertificationResult> certs;
	private List<CQMResult> cqms;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
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
	public String getCertifyingBody() {
		return certifyingBody;
	}
	public void setCertifyingBody(String certifyingBody) {
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
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
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
