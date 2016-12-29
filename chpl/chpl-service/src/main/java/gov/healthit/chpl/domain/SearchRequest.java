package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchRequest implements Serializable {
	private static final long serialVersionUID = 1179207628639701580L;

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
	//AND these
	Set<SurveillanceSearchOptions> surveillance = new HashSet<SurveillanceSearchOptions>();
	
	Boolean hasHadSurveillance;
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
	public Set<SurveillanceSearchOptions> getSurveillance() {
		return surveillance;
	}
	public void setSurveillance(Set<SurveillanceSearchOptions> surveillance) {
		this.surveillance = surveillance;
	}
	public Boolean getHasHadSurveillance() {
		return hasHadSurveillance;
	}
	public void setHasHadSurveillance(Boolean hasHadSurveillance) {
		this.hasHadSurveillance = hasHadSurveillance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((searchTerm == null) ? 0 : searchTerm.hashCode());
		result = prime * result
				+ ((certificationStatuses == null) ? 0 : certificationStatuses.hashCode());
		result = prime * result + ((certificationEditions == null) ? 0 : certificationEditions.hashCode());
		result = prime * result
				+ ((certificationCriteria == null) ? 0 : certificationCriteria.hashCode());
		result = prime * result
				+ ((cqms == null) ? 0 : cqms.hashCode());
		result = prime * result
				+ ((certificationBodies == null) ? 0 : certificationBodies.hashCode());
		result = prime * result
				+ ((surveillance == null) ? 0 : surveillance.hashCode());
		result = prime * result
				+ ((certificationBodies == null) ? 0 : certificationBodies.hashCode());
		result = prime * result
				+ ((hasHadSurveillance == null) ? 0 : hasHadSurveillance.hashCode());
		result = prime * result
				+ ((developer == null) ? 0 : developer.hashCode());
		result = prime * result
				+ ((product == null) ? 0 : product.hashCode());
		result = prime * result
				+ ((version == null) ? 0 : version.hashCode());
		result = prime * result
				+ ((practiceType == null) ? 0 : practiceType.hashCode());
		result = prime * result
				+ ((certificationDateStart == null) ? 0 : certificationDateStart.hashCode());
		result = prime * result
				+ ((certificationDateEnd == null) ? 0 : certificationDateEnd.hashCode());
		result = prime * result
				+ ((orderBy == null) ? 0 : orderBy.hashCode());
		result = prime * result
				+ ((sortDescending == null) ? 0 : sortDescending.hashCode());
		result = prime * result
				+ ((pageNumber == null) ? 0 : pageNumber.hashCode());
		result = prime * result
				+ ((pageSize == null) ? 0 : pageSize.hashCode());
		result = prime * result
				+ ((practiceType == null) ? 0 : practiceType.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchRequest other = (SearchRequest) obj;
		return this.hashCode() == other.hashCode();
	}
}
