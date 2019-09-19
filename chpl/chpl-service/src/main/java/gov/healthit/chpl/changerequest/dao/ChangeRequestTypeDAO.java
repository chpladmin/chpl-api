package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ChangeRequestTypeDAO {
    ChangeRequestType getChangeRequestTypeById(Long changeRequestTypeId) throws EntityRetrievalException;

    List<ChangeRequestType> getChangeRequestTypes();
}
