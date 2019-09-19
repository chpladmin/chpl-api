package gov.healthit.chpl.changerequest.dao;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestWebsiteDAO {
    ChangeRequestWebsite create(final ChangeRequest cr, final ChangeRequestWebsite crWebsite)
            throws EntityRetrievalException;

    ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException;

    ChangeRequestWebsite update(final ChangeRequestWebsite crWebsite) throws EntityRetrievalException;
}
