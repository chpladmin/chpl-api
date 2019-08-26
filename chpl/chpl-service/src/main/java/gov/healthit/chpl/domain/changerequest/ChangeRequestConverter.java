package gov.healthit.chpl.domain.changerequest;

import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestTypeEntity;

public class ChangeRequestConverter {

    public static ChangeRequestStatusType convert(ChangeRequestStatusTypeEntity entity) {
        ChangeRequestStatusType status = new ChangeRequestStatusType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public static ChangeRequestType convert(ChangeRequestTypeEntity entity) {
        ChangeRequestType status = new ChangeRequestType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

}
