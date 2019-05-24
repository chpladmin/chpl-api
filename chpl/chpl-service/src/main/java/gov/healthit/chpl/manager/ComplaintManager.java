package gov.healthit.chpl.manager;

import java.util.Set;

import gov.healthit.chpl.domain.KeyValueModel;

public interface ComplaintManager {
    Set<KeyValueModel> getComplaintTypes();

    Set<KeyValueModel> getComplaintStatusTypes();
}
