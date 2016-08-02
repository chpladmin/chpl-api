package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;

import java.util.Set;

public interface SearchMenuManager {
	
	public Set<KeyValueModel> getClassificationNames();
	public Set<KeyValueModel> getEditionNames(Boolean simple);
	public Set<KeyValueModel> getCertificationStatuses();
	public Set<KeyValueModel> getPracticeTypeNames();
	public Set<KeyValueModel> getProductNames();
	public Set<KeyValueModel> getDeveloperNames();
	public Set<KeyValueModel> getCertBodyNames();
	public Set<KeyValueModel> getAccessibilityStandards();
	public Set<KeyValueModel> getUcdProcesses();
	public Set<KeyValueModel> getQmsStandards();
	public Set<KeyValueModel> getTargetedUesrs();
	public Set<KeyValueModel> getEducationTypes();
	public Set<KeyValueModel> getAgeRanges();
	public Set<KeyValueModel> getTestFunctionality();
	public Set<KeyValueModel> getTestStandards();
	public Set<KeyValueModel> getTestTools();
	public Set<DescriptiveModel> getCertificationCriterionNumbers(Boolean simple) throws EntityRetrievalException;
	public Set<DescriptiveModel> getCQMCriterionNumbers(Boolean simple);
	public PopulateSearchOptions getPopulateSearchOptions(Boolean simple) throws EntityRetrievalException;
	
}
