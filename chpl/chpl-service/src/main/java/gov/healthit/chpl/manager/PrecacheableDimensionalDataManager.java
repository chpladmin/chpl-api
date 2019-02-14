package gov.healthit.chpl.manager;

import java.util.Set;

import gov.healthit.chpl.domain.KeyValueModelStatuses;

public interface PrecacheableDimensionalDataManager {
    Set<KeyValueModelStatuses> getProductNamesCached();
    Set<KeyValueModelStatuses> getProductNames();
    Set<KeyValueModelStatuses> getDeveloperNamesCached();
    Set<KeyValueModelStatuses> getDeveloperNames();
}
