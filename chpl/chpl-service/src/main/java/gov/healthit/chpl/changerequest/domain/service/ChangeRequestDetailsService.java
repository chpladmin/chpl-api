package gov.healthit.chpl.changerequest.domain.service;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDetailsService<T> {
    T getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException;

    ChangeRequest create(final ChangeRequest cr);

    ChangeRequest update(final ChangeRequest cr);

    ChangeRequest postStatusChangeProcessing(final ChangeRequest cr);
}
