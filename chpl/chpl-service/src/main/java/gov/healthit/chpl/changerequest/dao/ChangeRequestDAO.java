package gov.healthit.chpl.changerequest.dao;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDAO {
    ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException;

    ChangeRequest get(final Long changeRequestId) throws EntityRetrievalException;
}
