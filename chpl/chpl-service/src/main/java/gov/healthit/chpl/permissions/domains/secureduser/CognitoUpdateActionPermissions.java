package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Component("securedCognitoUserUpdateActionPermissions")
@Log4j2
public class CognitoUpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return getResourcePermissions().hasPermissionOnUser(user);
        } else {
            return false;
        }
    }
}
