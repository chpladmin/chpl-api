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
	public Set<DescriptiveModel> getCertificationCriterionNumbers(Boolean simple) throws EntityRetrievalException;
	public Set<DescriptiveModel> getCQMCriterionNumbers(Boolean simple);
	public PopulateSearchOptions getPopulateSearchOptions(Boolean simple) throws EntityRetrievalException;
	
}
