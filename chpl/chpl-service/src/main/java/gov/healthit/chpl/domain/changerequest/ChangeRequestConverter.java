package gov.healthit.chpl.domain.changerequest;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.changerequest.ChangeRequestCertificationBodyMapEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestTypeEntity;

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
        // Ugh...
        cr.setDeveloper(new Developer(new DeveloperDTO(entity.getDeveloper())));
        return cr;
    }

    public static ChangeRequestCertificationBodyMap convert(final ChangeRequestCertificationBodyMapEntity entity) {
        ChangeRequestCertificationBodyMap map = new ChangeRequestCertificationBodyMap();
        map.setId(map.getId());
        map.setChangeRequest(convert(entity.getChangeRequest()));
        // Ugh...
        map.setCertificationBody(new CertificationBody(new CertificationBodyDTO(entity.getCertificationBody())));
        return map;
    }

    public static ChangeRequestStatus convert(ChangeRequestStatusEntity entity) {
        ChangeRequestStatus status = new ChangeRequestStatus();
        status.setId(entity.getId());
        status.setChangeRequestStatusType(convert(entity.getChangeRequestStatusType()));
        status.setCommment(entity.getComment());
        status.setStatusChangeDate(entity.getStatusChangeDate());
        // Ugh...
        if (entity.getCertificationBody() != null) {
            status.setCertificationBody(new CertificationBody(new CertificationBodyDTO(entity.getCertificationBody())));
        }
        return status;
    }
}
