package gov.healthit.chpl.permissions.domains.announcement;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("announcementGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleAtlAdmin()
                || getResourcePermissions().isUserRoleCmsStaff();
    }

    /**
     * Anonymous users + ROLE_DEVELOPER can only see public announcements.
     * Other logged-in users can see all public/private announcements.
     */
    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof AnnouncementDTO)) {
            return false;
        }
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleAtlAdmin()
                || getResourcePermissions().isUserRoleCmsStaff()) {
            return true;
        } else {
            AnnouncementDTO announcement = (AnnouncementDTO) obj;
            if (announcement.getIsPublic()) {
                return true;
            }
            return false;
        }
    }

}
