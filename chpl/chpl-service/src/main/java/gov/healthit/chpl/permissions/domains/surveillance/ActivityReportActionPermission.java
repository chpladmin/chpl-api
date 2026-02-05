package gov.healthit.chpl.permissions.domains.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceActivityReportActionPermission")
public class ActivityReportActionPermission extends ActionPermissions {

    @Autowired
    public ActivityReportActionPermission(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
       return getResourcePermissions().isUserRoleAdmin()
               || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        // Not used
        return false;
    }
}
