package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchFilters {
	
	String vendor = null;
	String product = null;
	String version = null;
	List<String> certificationCriteria = new ArrayList<String>();
	List<String> cqms = new ArrayList<String>();
	String certificationEdition = null;
	String productClassification = null;
	String practiceType = null;
	
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
	public List<String> getCertificationCriteria() {
		return certificationCriteria;
	}
	public void setCertificationCriteria(List<String> certificationCriteria) {
		this.certificationCriteria = certificationCriteria;
	}
	public void addCertificationCriteria(String certificationCriteria) {
		this.certificationCriteria.add(certificationCriteria);
	}
	public List<String> getCqms() {
		return cqms;
	}
	public void setCqm(List<String> cqms) {
		this.cqms = cqms;
	}
	public String getCertificationEdition() {
		return certificationEdition;
	}
	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}
	public String getProductClassification() {
		return productClassification;
	}
	public void setProductClassification(String productClassification) {
		this.productClassification = productClassification;
	}
	public String getPracticeType() {
		return practiceType;
	}
	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}
	
}
