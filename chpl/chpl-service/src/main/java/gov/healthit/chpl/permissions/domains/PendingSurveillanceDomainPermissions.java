package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.pendingsurveillance.ConfirmActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.RejectActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingsurveillance.UploadActionPermissions;

@Component
public class PendingSurveillanceDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String UPLOAD = "UPLOAD";
    public static final String REJECT = "REJECT";
    public static final String CONFIRM = "CONFIRM";

    @Autowired
    public PendingSurveillanceDomainPermissions(final GetAllActionPermissions getAllActionPermissions,
            final UploadActionPermissions uploadActionPermissions,
            final RejectActionPermissions rejectActionPermissions,
            final ConfirmActionPermissions confirmActionPermissions) {
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(UPLOAD, uploadActionPermissions);
        getActionPermissions().put(REJECT, rejectActionPermissions);
        getActionPermissions().put(CONFIRM, confirmActionPermissions);
    }
}
