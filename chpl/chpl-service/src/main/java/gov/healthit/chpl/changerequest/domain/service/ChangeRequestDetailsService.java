package gov.healthit.chpl.changerequest.domain.service;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDetailsService<T> {
    T getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException;

    ChangeRequest create(ChangeRequest cr);

    ChangeRequest update(ChangeRequest cr);

    ChangeRequest postStatusChangeProcessing(ChangeRequest cr);
}
