package gov.healthit.chpl.domain;


public class CertifiedProductSearchResult {
	
	
	private Long id;
	private String vendor;
	private String product;
	private String version;
	private String chplNum;
	private String certsAndCQMs;
	private String practiceType;
	private String classification;
	private String certifyingBody;
	private String certificationEdition;
	
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
	public String getChplNum() {
		return chplNum;
	}
	public void setChplNum(String chplNum) {
		this.chplNum = chplNum;
	}
	public String getCertsAndCQMs() {
		return certsAndCQMs;
	}
	public void setCertsAndCQMs(String certsAndCQMs) {
		this.certsAndCQMs = certsAndCQMs;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
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

}
