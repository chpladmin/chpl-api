package gov.healthit.chpl.dao.changerequest;

import java.util.List;

import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestStatusTypeDAO {
    ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId)
            throws EntityRetrievalException;

    List<ChangeRequestStatusType> getChangeRequestStatusTypes();
}
