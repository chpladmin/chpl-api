package gov.healthit.chpl.permissions.domains.listingUpload;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("createListingUploadActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj instanceof ListingUpload) {
            return hasAccess((ListingUpload) obj);
        } else if (obj instanceof List<?>) {
            boolean hasAccessToAll = true;
            for (Object listItem : (List<?>) obj) {
                if (listItem instanceof ListingUpload) {
                    hasAccessToAll = hasAccessToAll && hasAccess((ListingUpload) listItem);
                } else {
                    hasAccessToAll = false;
                }
            }
            return hasAccessToAll;
        }
        return false;
    }

    private boolean hasAccess(ListingUpload uploadedMetadata) {
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            return isAcbValidForCurrentUser(uploadedMetadata.getAcb().getId());
        }
        return false;
    }
}
