package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDAO {
    ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException;

    ChangeRequest get(final Long changeRequestId) throws EntityRetrievalException;

    List<ChangeRequest> getAllForCurrentUser() throws EntityRetrievalException;

    List<ChangeRequest> getByDeveloper(final Long developerId) throws EntityRetrievalException;
}
