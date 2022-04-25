package gov.healthit.chpl.permissions.domains.announcement;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("announcementGetAllIncludingFutureActionPermissions")
public class GetAllIncludingFutureActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Announcement)) {
            return false;
        }
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

}
