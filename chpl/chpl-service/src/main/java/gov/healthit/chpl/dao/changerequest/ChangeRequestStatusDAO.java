package gov.healthit.chpl.dao.changerequest;

import java.util.List;

import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestStatusDAO {
    ChangeRequestStatus create(final ChangeRequest cr, final ChangeRequestStatus crStatus)
            throws EntityRetrievalException;

    List<ChangeRequestStatus> getByChangeRequestId(final Long changeRequestId);
}
