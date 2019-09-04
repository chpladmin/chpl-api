package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestCertificationBodyMapDAO {
    ChangeRequestCertificationBodyMap create(final ChangeRequestCertificationBodyMap map)
            throws EntityRetrievalException;

    List<ChangeRequestCertificationBodyMap> getByChangeRequestId(final Long changeRequestId);
}
