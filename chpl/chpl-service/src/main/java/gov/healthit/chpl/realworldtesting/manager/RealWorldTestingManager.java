package gov.healthit.chpl.realworldtesting.manager;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public class RealWorldTestingManager {

    @Transactional
    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
    //        + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).UPLOAD)")
    public void  uploadPendingSurveillance(MultipartFile file) {

    }

}
