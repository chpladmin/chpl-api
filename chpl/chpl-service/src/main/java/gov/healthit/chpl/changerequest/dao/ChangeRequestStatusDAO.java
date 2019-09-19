package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestStatusDAO {
    ChangeRequestStatus create(final ChangeRequest cr, final ChangeRequestStatus crStatus)
            throws EntityRetrievalException;

    List<ChangeRequestStatus> getByChangeRequestId(final Long changeRequestId);
}
