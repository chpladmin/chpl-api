package gov.healthit.chpl.permissions.domains.listingUpload;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("getListingUploadAsListingActionPermissions")
public class GetListingUploadAsListingPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj instanceof ListingUpload) {
            ListingUpload listingUpload = (ListingUpload) obj;
            return hasAccess(listingUpload);
        }
        return false;
    }

    private boolean hasAccess(ListingUpload uploadedMetadata) {
        if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            return isAcbValidForCurrentUser(uploadedMetadata.getAcb().getId());
        }
        return false;
    }
}
