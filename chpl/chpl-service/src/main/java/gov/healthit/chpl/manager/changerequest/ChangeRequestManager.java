package gov.healthit.chpl.manager.changerequest;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestManager {
    ChangeRequest createWebsiteChangeRequest(final Developer developer, final String website)
            throws EntityRetrievalException;

    ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException;
}
