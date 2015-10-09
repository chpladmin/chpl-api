package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SimpleModel;

import java.util.Set;

public interface SearchMenuManager {
	
	public Set<SimpleModel> getClassificationNames();
	public Set<SimpleModel> getEditionNames();
	public Set<SimpleModel> getCertificationStatuses();
	public Set<SimpleModel> getPracticeTypeNames();
	public Set<SimpleModel> getProductNames();
	public Set<SimpleModel> getVendorNames();
	public Set<SimpleModel> getCertBodyNames();
	public Set<SimpleModel> getCertificationCriterionNumbers();
	public Set<SimpleModel> getCQMCriterionNumbers();
	public PopulateSearchOptions getPopulateSearchOptions();
	
}
