package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.PopulateSearchOptions;

import java.util.Set;

public interface SearchMenuManager {
	
	public Set<String> getClassificationNames();
	public Set<String> getEditionNames();
	public Set<String> getPracticeTypeNames();
	public Set<String> getProductNames();
	public Set<String> getVendorNames();
	public Set<String> getCertBodyNames();
	public Set<String> getCertificationCriterionNumbers();
	public Set<String> getCQMCriterionNumbers();
	public PopulateSearchOptions getPopulateSearchOptions();
	
}
