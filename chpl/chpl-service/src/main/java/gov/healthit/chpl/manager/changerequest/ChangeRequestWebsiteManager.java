package gov.healthit.chpl.manager.changerequest;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestWebsiteManager {
    ChangeRequestWebsite create(Developer developer, String website) throws EntityRetrievalException;
}
