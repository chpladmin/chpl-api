package gov.healthit.chpl.domain;

import java.util.Set;

public class PopulateSearchOptions {

	private Set<SimpleModel> productClassifications;
	private Set<SimpleModel> editions;
	private Set<SimpleModel> certificationStatuses;
	private Set<SimpleModel> practiceTypeNames;
	private Set<SimpleModel> productNames;
	private Set<SimpleModel> vendorNames;
	private Set<SimpleModel> certBodyNames;
	private Set<SimpleModel> certificationCriterionNumbers;
	private Set<SimpleModel> cqmCriterionNumbers;
	public Set<SimpleModel> getProductClassifications() {
		return productClassifications;
	}
	public void setProductClassifications(Set<SimpleModel> productClassifications) {
		this.productClassifications = productClassifications;
	}
	public Set<SimpleModel> getEditions() {
		return editions;
	}
	public void setEditions(Set<SimpleModel> editions) {
		this.editions = editions;
	}
	public Set<SimpleModel> getPracticeTypeNames() {
		return practiceTypeNames;
	}
	public void setPracticeTypeNames(Set<SimpleModel> practiceTypeNames) {
		this.practiceTypeNames = practiceTypeNames;
	}
	public Set<SimpleModel> getProductNames() {
		return productNames;
	}
	public void setProductNames(Set<SimpleModel> productNames) {
		this.productNames = productNames;
	}
	public Set<SimpleModel> getVendorNames() {
		return vendorNames;
	}
	public void setVendorNames(Set<SimpleModel> vendorNames) {
		this.vendorNames = vendorNames;
	}
	public Set<SimpleModel> getCertBodyNames() {
		return certBodyNames;
	}
	public void setCertBodyNames(Set<SimpleModel> certBodyNames) {
		this.certBodyNames = certBodyNames;
	}
	public Set<SimpleModel> getCertificationCriterionNumbers() {
		return certificationCriterionNumbers;
	}
	public void setCertificationCriterionNumbers(Set<SimpleModel> certificationCriterionNumbers) {
		this.certificationCriterionNumbers = certificationCriterionNumbers;
	}
	public Set<SimpleModel> getCqmCriterionNumbers() {
		return cqmCriterionNumbers;
	}
	public void setCqmCriterionNumbers(Set<SimpleModel> cqmCriterionNumbers) {
		this.cqmCriterionNumbers = cqmCriterionNumbers;
	}
	public Set<SimpleModel> getCertificationStatuses() {
		return certificationStatuses;
	}
	public void setCertificationStatuses(Set<SimpleModel> certificationStatuses) {
		this.certificationStatuses = certificationStatuses;
	}
	

}
