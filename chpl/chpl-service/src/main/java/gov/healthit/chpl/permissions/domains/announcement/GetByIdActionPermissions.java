package gov.healthit.chpl.permissions.domains.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("announcementGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    private AnnouncementDAO announcementDao;

    @Autowired
    public GetByIdActionPermissions(final AnnouncementDAO announcementDao) {
        this.announcementDao = announcementDao;
    }

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
        if (!(obj instanceof Long)) {
            return false;
        }
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleAtlAdmin()
                || getResourcePermissions().isUserRoleCmsStaff()) {
            return true;
        } else {
            try {
                AnnouncementDTO announcement = announcementDao.getById((Long) obj, true);
                if (announcement.getIsPublic()) {
                    return true;
                }
            } catch (EntityRetrievalException ex) {
                return false;
            }
            return false;
        }
    }

}
