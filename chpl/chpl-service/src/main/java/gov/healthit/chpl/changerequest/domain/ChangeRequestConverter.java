package gov.healthit.chpl.changerequest.domain;

import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestWebsiteEntity;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;

public class ChangeRequestConverter {

    public static ChangeRequestStatusType convert(final ChangeRequestStatusTypeEntity entity) {
        ChangeRequestStatusType status = new ChangeRequestStatusType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public static ChangeRequestType convert(final ChangeRequestTypeEntity entity) {
        ChangeRequestType status = new ChangeRequestType();
        status.setId(entity.getId());
        status.setName(entity.getName());
        return status;
    }

    public static ChangeRequest convert(final ChangeRequestEntity entity) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(entity.getId());
        cr.setChangeRequestType(convert(entity.getChangeRequestType()));
        cr.setDeveloper(new Developer(new DeveloperDTO(entity.getDeveloper())));
        return cr;
    }

    public static ChangeRequestStatus convert(ChangeRequestStatusEntity entity) {
        ChangeRequestStatus status = new ChangeRequestStatus();
        status.setId(entity.getId());
        status.setChangeRequestStatusType(convert(entity.getChangeRequestStatusType()));
        status.setComment(entity.getComment());
        status.setStatusChangeDate(entity.getStatusChangeDate());
        if (entity.getCertificationBody() != null) {
            status.setCertificationBody(new CertificationBody(new CertificationBodyDTO(entity.getCertificationBody())));
        }
        return status;
    }

    public static ChangeRequestWebsite convert(ChangeRequestWebsiteEntity entity) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        crWebsite.setId(entity.getId());
        crWebsite.setWebsite(entity.getWebsite());
        return crWebsite;
    }
}
