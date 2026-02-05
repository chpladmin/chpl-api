package gov.healthit.chpl.permissions.domains.listingUpload;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("createListingUploadActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Autowired
    public CreateActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
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
        if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin() && uploadedMetadata.getAcb() != null) {
            return isAcbValidForCurrentUser(uploadedMetadata.getAcb().getId());
        }
        return false;
    }
}
