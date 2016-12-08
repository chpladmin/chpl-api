package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
	public static final String HAS_OPEN_CAP = "open";
	public static final String HAS_CLOSED_CAP = "closed";
	public static final String NEVER_HAD_CAP = "never";
	public static final String CERTIFICATION_DATE_SEARCH_FORMAT = "yyyy-MM-dd";
	
	String searchTerm = null;
	
	//search for any of these
	List<String> certificationStatuses = new ArrayList<String>();
	//search for any of these
	List<String> certificationEditions = new ArrayList<String>();
	//search for all of these
	List<String> certificationCriteria = new ArrayList<String>();
	//search for all of these
	List<String> cqms = new ArrayList<String>();
	//search for any of these
	List<String> certificationBodies = new ArrayList<String>();
	//search for any of these, can be any of the static string values above
	List<String> correctiveActionPlans = new ArrayList<String>();
	
	String developer = null;
	String product = null;
	String version = null;
	String practiceType = null;
	String certificationDateStart = null;
	String certificationDateEnd = null;
	
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
	public List<String> getCertificationStatuses() {
		return certificationStatuses;
	}
	public void setCertificationStatuses(List<String> certificationStatuses) {
		this.certificationStatuses = certificationStatuses;
	}
	public List<String> getCertificationEditions() {
		return certificationEditions;
	}
	public void setCertificationEditions(List<String> certificationEditions) {
		this.certificationEditions = certificationEditions;
	}
	public List<String> getCertificationCriteria() {
		return certificationCriteria;
	}
	public void setCertificationCriteria(List<String> certificationCriteria) {
		this.certificationCriteria = certificationCriteria;
	}
	public List<String> getCqms() {
		return cqms;
	}
	public void setCqms(List<String> cqms) {
		this.cqms = cqms;
	}
	public List<String> getCertificationBodies() {
		return certificationBodies;
	}
	public void setCertificationBodies(List<String> certificationBodies) {
		this.certificationBodies = certificationBodies;
	}
	public List<String> getCorrectiveActionPlans() {
		return correctiveActionPlans;
	}
	public void setCorrectiveActionPlans(List<String> correctiveActionPlans) {
		this.correctiveActionPlans = correctiveActionPlans;
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
	public Boolean getSortDescending() {
		return sortDescending;
	}
	public void setSortDescending(Boolean sortDescending) {
		this.sortDescending = sortDescending;
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
	public String getCertificationDateStart() {
		return certificationDateStart;
	}
	public void setCertificationDateStart(String certificationDateStart) {
		this.certificationDateStart = certificationDateStart;
	}
	public String getCertificationDateEnd() {
		return certificationDateEnd;
	}
	public void setCertificationDateEnd(String certificationDateEnd) {
		this.certificationDateEnd = certificationDateEnd;
	}
}
