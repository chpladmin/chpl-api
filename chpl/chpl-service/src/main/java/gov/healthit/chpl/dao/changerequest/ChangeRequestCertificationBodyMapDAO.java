package gov.healthit.chpl.dao.changerequest;

import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestCertificationBodyMapDAO {
    ChangeRequestCertificationBodyMap create(final ChangeRequestCertificationBodyMap map)
            throws EntityRetrievalException;

}
