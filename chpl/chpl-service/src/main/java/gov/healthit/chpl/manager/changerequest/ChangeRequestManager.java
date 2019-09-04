package gov.healthit.chpl.manager.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestManager {
    ChangeRequest createWebsiteChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException;

    ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException;

    ChangeRequest updateChangeRequest(final ChangeRequest cr) throws EntityRetrievalException;
}
