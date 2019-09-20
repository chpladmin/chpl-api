package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDAO {
    ChangeRequest create(ChangeRequest cr) throws EntityRetrievalException;

    ChangeRequest get(Long changeRequestId) throws EntityRetrievalException;

    List<ChangeRequest> getAllForCurrentUser() throws EntityRetrievalException;

    List<ChangeRequest> getAllPending() throws EntityRetrievalException;

    List<ChangeRequest> getByDeveloper(Long developerId) throws EntityRetrievalException;
}
