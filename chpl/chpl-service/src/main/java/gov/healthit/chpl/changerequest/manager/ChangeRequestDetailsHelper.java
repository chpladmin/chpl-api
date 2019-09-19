package gov.healthit.chpl.changerequest.manager;

import java.util.HashMap;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestDetailsHelper<T> {
    T getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException;

    T getDetailsFromHashMap(final HashMap<String, Object> map);

    T create(final ChangeRequest cr, final T details);

    T update(final ChangeRequest cr, final T details);

    void execute(final ChangeRequest cr) throws EntityRetrievalException, EntityCreationException;

}
