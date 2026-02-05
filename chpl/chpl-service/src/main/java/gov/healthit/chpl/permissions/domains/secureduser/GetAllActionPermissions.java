package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Autowired
    public GetAllActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return getResourcePermissions().hasPermissionOnUser(user);
        }
        return false;
    }
}
