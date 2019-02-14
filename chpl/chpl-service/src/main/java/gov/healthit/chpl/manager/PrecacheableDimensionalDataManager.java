package gov.healthit.chpl.manager;

import java.util.Set;

import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface PrecacheableDimensionalDataManager {
    Set<KeyValueModel> getClassificationNames();
    Set<KeyValueModel> getEditionNames(final Boolean simple);
    Set<KeyValueModel> getCertificationStatuses();
    Set<KeyValueModel> getPracticeTypeNames();
    Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple);
    Set<DescriptiveModel> getCertificationCriterionNumbers(final Boolean simple) throws EntityRetrievalException;
    Set<KeyValueModelStatuses> getProductNamesCached();
    Set<KeyValueModelStatuses> getProductNames();
    Set<KeyValueModelStatuses> getDeveloperNamesCached();
    Set<KeyValueModelStatuses> getDeveloperNames();
}
