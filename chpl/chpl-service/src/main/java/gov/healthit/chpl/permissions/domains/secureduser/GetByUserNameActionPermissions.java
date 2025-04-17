package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Component("securedUserGetByUserNameActionPermisions")
@Log4j2
public class GetByUserNameActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return !getResourcePermissions().isUserAnonymous();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj == null) {
            return true;
        }

        return doesCurrentUserHavePermissionToSubjectUser((User) obj);
    }

    private boolean doesCurrentUserHavePermissionToSubjectUser(User user) {
        return getResourcePermissions().hasPermissionOnUser(user);
    }
}
