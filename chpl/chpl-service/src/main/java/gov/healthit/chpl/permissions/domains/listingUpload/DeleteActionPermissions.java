package gov.healthit.chpl.permissions.domains.listingUpload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.upload.listing.ListingUploadDao;

@Component("deleteListingUploadActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    private ListingUploadDao listingUploadDao;

    @Autowired
    public DeleteActionPermissions(ListingUploadDao listingUploadDao) {
        this.listingUploadDao = listingUploadDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj instanceof Long) {
            Long listingUploadId = (Long) obj;
            try {
                ListingUpload listingUpload = listingUploadDao.getById(listingUploadId);
                return hasAccess(listingUpload);
            } catch (EntityRetrievalException ex) {
                return false;
            }
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
