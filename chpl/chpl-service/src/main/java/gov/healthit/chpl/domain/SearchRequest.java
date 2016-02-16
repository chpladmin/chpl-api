package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
	public static final String CURRENT_CAP = "current";
	public static final String CLOSED_CAP = "closed";
	public static final String NO_CAP = "never";
	public static final String ANY_CAP = "all";
	
	String searchTerm = null;
	String developer = null;
	String product = null;
	String version = null;
	List<String> certificationCriteria = new ArrayList<String>();
	List<String> cqms = new ArrayList<String>();
	String certificationEdition = null;
	String certificationBody = null;
	String productClassification = null;
	String practiceType = null;
	String visibleOnCHPL = "YES";
	String hasCAP = "BOTH";
	String orderBy = "product";
	Boolean sortDescending = false;
	Integer pageNumber = 0;
	Integer pageSize = 20;
	
	
	public String getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	public String getDeveloper() {
		return developer;
	}
	public void setDeveloper(String developer) {
		this.developer = developer;
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
	public void setCqms(List<String> cqms) {
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
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getCertificationBody() {
		return certificationBody;
	}
	public void setCertificationBody(String certifyingBody) {
		this.certificationBody = certifyingBody;
	}
	public Boolean getSortDescending() {
		return sortDescending;
	}
	public void setSortDescending(Boolean sortDescending) {
		this.sortDescending = sortDescending;
	}
	public String getVisibleOnCHPL() {
		return visibleOnCHPL;
	}
	public void setVisibleOnCHPL(String visibleOnCHPL) {
		this.visibleOnCHPL = visibleOnCHPL;
	}
	public String getHasCAP() {
		return hasCAP;
	}
	public void setHasCAP(String hasCAP) {
		this.hasCAP = hasCAP;
	}
	public Integer getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
}
