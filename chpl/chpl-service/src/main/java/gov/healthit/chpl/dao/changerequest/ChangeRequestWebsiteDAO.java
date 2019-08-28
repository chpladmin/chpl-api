package gov.healthit.chpl.dao.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestWebsiteDAO {
    ChangeRequestWebsite create(ChangeRequestWebsite crWebSite) throws EntityRetrievalException;
}
