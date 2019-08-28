package gov.healthit.chpl.dao.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDAO {
    ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException;

    ChangeRequest get(final Long changeRequestId) throws EntityRetrievalException;
}
