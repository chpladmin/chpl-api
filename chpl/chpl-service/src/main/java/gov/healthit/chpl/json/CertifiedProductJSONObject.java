package gov.healthit.chpl.json;

import java.util.Date;
import java.util.List;

public class CertifiedProductJSONObject {
	
	
	private Long id;
	private String additionalSoftware;
	private String vendor;
	private String product;
	private String version;
	private String edition;
	private Date certDate;
	private Date lastModifiedDate;
	private List<ModificationItemJSONObject> lastModifiedItems;
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
	public String getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(String additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
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
	public List<ModificationItemJSONObject> getLastModifiedItems() {
		return lastModifiedItems;
	}
	public void setLastModifiedItems(
			List<ModificationItemJSONObject> lastModifiedItems) {
		this.lastModifiedItems = lastModifiedItems;
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
