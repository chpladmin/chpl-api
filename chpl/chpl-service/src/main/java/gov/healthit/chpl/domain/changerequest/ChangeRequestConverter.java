package gov.healthit.chpl.domain.changerequest;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestTypeEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;

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

    public static ChangeRequestTypeEntity convert(ChangeRequestType type) {
        ChangeRequestTypeEntity status = new ChangeRequestTypeEntity();
        status.setId(type.getId());
        status.setName(type.getName());
        return status;
    }

    public static ChangeRequestEntity convert(ChangeRequest changeRequest) {
        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setId(changeRequest.getId());
        entity.setChangeRequestType(convert(changeRequest.getChangeRequestType()));
        entity.setDeveloper(new DeveloperEntity(changeRequest.getDeveloper().getDeveloperId()));
        return entity;
    }

    public static ChangeRequest convert(ChangeRequestEntity entity) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(entity.getId());
        cr.setChangeRequestType(convert(entity.getChangeRequestType()));
        cr.setDeveloper(new Developer(new DeveloperDTO(entity.getDeveloper())));

        return cr;
    }
}
