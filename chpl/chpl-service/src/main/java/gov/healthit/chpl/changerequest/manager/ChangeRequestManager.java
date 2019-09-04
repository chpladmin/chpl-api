package gov.healthit.chpl.changerequest.manager;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestManager {
    ChangeRequest createWebsiteChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException;

    ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException;

    ChangeRequest updateChangeRequest(final ChangeRequest cr) throws EntityRetrievalException;
}
