package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.PopulateSearchOptions;

import java.util.List;

public interface SearchMenuManager {
	
	public List<String> getClassificationNames();
	public List<String> getEditionNames();
	public List<String> getPracticeTypeNames();
	public List<String> getProductNames();
	public List<String> getVendorNames();
	public List<String> getCertBodyNames();
	public PopulateSearchOptions getPopulateSearchOptions();
	
}
