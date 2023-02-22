package gov.healthit.chpl.permissions.domains.listingUpload;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("getUploadedCsvActionPermissions")
public class GetUploadedCsvActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
