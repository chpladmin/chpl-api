package gov.healthit.chpl.dao.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestWebsiteDAO {
    ChangeRequestWebsite create(final ChangeRequest cr, final ChangeRequestWebsite crWebsite)
            throws EntityRetrievalException;

    ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException;
}
