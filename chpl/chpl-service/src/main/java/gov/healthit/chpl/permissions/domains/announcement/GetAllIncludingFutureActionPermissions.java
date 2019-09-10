package gov.healthit.chpl.permissions.domains.announcement;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("announcementGetAllIncludingFutureActionPermissions")
public class GetAllIncludingFutureActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof AnnouncementDTO)) {
            return false;
        }
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

}
