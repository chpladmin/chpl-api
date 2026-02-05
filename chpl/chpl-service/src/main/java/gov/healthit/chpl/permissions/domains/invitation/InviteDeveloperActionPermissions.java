package gov.healthit.chpl.permissions.domains.invitation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("invitationInviteDeveloperActionPermissions")
public class InviteDeveloperActionPermissions extends ActionPermissions {

    @Autowired
    public InviteDeveloperActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            Long developerId = (Long) obj;
            return isDeveloperValidForCurrentUser(developerId);
        } else {
            return false;
        }
    }

}
