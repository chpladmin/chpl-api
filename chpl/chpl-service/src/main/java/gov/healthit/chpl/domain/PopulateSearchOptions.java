package gov.healthit.chpl.domain;

import java.util.Set;

public class PopulateSearchOptions {

	private Set<String> productClassifications;
	private Set<String> editions;
	private Set<String> practiceTypeNames;
	private Set<String> productNames;
	private Set<String> vendorNames;
	private Set<String> certBodyNames;
	
	
	
	public Set<String> getProductClassifications() {
		return productClassifications;
	}
	public void setProductClassifications(Set<String> productClassifications) {
		this.productClassifications = productClassifications;
	}
	public Set<String> getEditions() {
		return editions;
	}
	public void setEditions(Set<String> editions) {
		this.editions = editions;
	}
	public Set<String> getPracticeTypeNames() {
		return practiceTypeNames;
	}
	public void setPracticeTypeNames(Set<String> practiceTypeNames) {
		this.practiceTypeNames = practiceTypeNames;
	}
	public Set<String> getProductNames() {
		return productNames;
	}
	public void setProductNames(Set<String> productNames) {
		this.productNames = productNames;
	}
	public Set<String> getVendorNames() {
		return vendorNames;
	}
	public void setVendorNames(Set<String> vendorNames) {
		this.vendorNames = vendorNames;
	}
	public Set<String> getCertBodyNames() {
		return certBodyNames;
	}
	public void setCertBodyNames(Set<String> certBodyNames) {
		this.certBodyNames = certBodyNames;
	}
	
}
