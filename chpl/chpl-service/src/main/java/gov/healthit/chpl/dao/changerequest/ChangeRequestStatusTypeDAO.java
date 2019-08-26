package gov.healthit.chpl.dao.changerequest;

import java.util.List;

import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;

public interface ChangeRequestStatusTypeDAO {
    ChangeRequestStatusType getChangeRequestStatusTypeById(Long changeRequestStatusTypeId);

    List<ChangeRequestStatusType> getChangeRequestStatusTypes();
}
