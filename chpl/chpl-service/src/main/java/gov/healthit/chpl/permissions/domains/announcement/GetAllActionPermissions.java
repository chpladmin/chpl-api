package gov.healthit.chpl.permissions.domains.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("announcementGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Autowired
    public GetAllActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleCmsStaff();
    }

    /**
     * Anonymous users + chpl-developer can only see public announcements.
     * Other logged-in users can see all public/private announcements.
     */
    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Announcement)) {
            return false;
        }
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleCmsStaff()) {
            return true;
        } else {
            Announcement announcement = (Announcement) obj;
            if (announcement.getIsPublic()) {
                return true;
            }
            return false;
        }
    }

}
