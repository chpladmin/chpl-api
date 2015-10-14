package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;

import java.util.Set;

public interface SearchMenuManager {
	
	public Set<KeyValueModel> getClassificationNames();
	public Set<KeyValueModel> getEditionNames();
	public Set<KeyValueModel> getCertificationStatuses();
	public Set<KeyValueModel> getPracticeTypeNames();
	public Set<KeyValueModel> getProductNames();
	public Set<KeyValueModel> getVendorNames();
	public Set<KeyValueModel> getCertBodyNames();
	public Set<DescriptiveModel> getCertificationCriterionNumbers();
	public Set<DescriptiveModel> getCQMCriterionNumbers();
	public PopulateSearchOptions getPopulateSearchOptions();
	
}
