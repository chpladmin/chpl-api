package gov.healthit.chpl.permissions.domains.apiKey;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("apiKeyDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

}
