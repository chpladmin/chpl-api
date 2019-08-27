package gov.healthit.chpl.dao.changerequest;

import java.util.List;

import gov.healthit.chpl.domain.changerequest.ChangeRequestType;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestTypeDAO {
    ChangeRequestType getChangeRequestTypeById(Long changeRequestTypeId) throws EntityRetrievalException;

    List<ChangeRequestType> getChangeRequestTypes();
}
