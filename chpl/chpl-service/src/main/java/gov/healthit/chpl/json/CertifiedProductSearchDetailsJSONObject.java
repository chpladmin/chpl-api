package gov.healthit.chpl.json;

import java.util.Date;
import java.util.List;

public class CertifiedProductSearchDetailsJSONObject {
	
	
	private Long id;
	private List<String> additionalSoftware;
	private String vendor;
	private String product;
	private String version;
	private String edition;
	private Date certDate;
	private Date lastModifiedDate;
	private ModificationItemJSONObject lastModifiedItem;
	private String practiceType;
	private String certBody;
	private List<CertificationJSONObject> certs;
	private String chplNum;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<String> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<String> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public void addAdditionalSoftware(String additionalSoftware){
		this.additionalSoftware.add(additionalSoftware);
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
	public ModificationItemJSONObject getLastModifiedItems() {
		return lastModifiedItem;
	}
	public void setLastModifiedItem(ModificationItemJSONObject lastModifiedItem) {
		this.lastModifiedItem = lastModifiedItem;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}
	public String getCertBody() {
		return certBody;
	}
	public void setCertBody(String certBody) {
		this.certBody = certBody;
	}
	public List<CertificationJSONObject> getCerts() {
		return certs;
	}
	public void setCerts(List<CertificationJSONObject> certs) {
		this.certs = certs;
	}
	public String getChplNum() {
		return chplNum;
	}
	public void setChplNum(String chplNum) {
		this.chplNum = chplNum;
	}
	
	

}
