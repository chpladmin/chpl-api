package gov.healthit.chpl.changerequest.manager;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDetailsHelper<T> {
    T getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException;

    // T getDetailsFromHashMap(final HashMap<String, Object> map);

    ChangeRequest create(final ChangeRequest cr);

    ChangeRequest update(final ChangeRequest cr);

    ChangeRequest postStatusChangeProcessing(final ChangeRequest cr);
}
