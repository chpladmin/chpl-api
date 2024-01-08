package gov.healthit.chpl.permissions.domains.subscription;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("subscriptionSearchActionPermissions")
public class SearchActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        return false;
    }

}
