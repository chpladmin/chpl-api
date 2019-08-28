package gov.healthit.chpl.manager.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestBaseManager {
    ChangeRequest create(ChangeRequest cr) throws EntityRetrievalException;
}
