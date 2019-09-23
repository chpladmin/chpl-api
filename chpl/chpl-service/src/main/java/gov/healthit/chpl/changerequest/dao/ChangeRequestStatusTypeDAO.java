package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestStatusTypeDAO {
    ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId)
            throws EntityRetrievalException;

    List<ChangeRequestStatusType> getChangeRequestStatusTypes();
}
