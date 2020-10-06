package gov.healthit.chpl.realworldtesting.manager;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public class RealWorldTestingManager {

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).REAL_WORLD_TESTING, "
            + "T(gov.healthit.chpl.permissions.domains.RealWorldTestingDomainPermissions).UPLOAD)")
    public void  uploadPendingSurveillance(MultipartFile file) {

    }

}
