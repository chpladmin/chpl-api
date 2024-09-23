package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("searchActivityActionPermissions")
public class SearchActivityActionPermissions extends ActionPermissions {

    public SearchActivityActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }
}
