package gov.healthit.chpl.manager.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestManager {
    ChangeRequest create(ChangeRequest cr) throws EntityRetrievalException;
}
