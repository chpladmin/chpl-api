package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.listingUpload.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.GetAllActionPermissions;

@Component
public class ListingUploadDomainPerissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String GET_ALL = "GET_ALL";
    public static final String DELETE = "DELETE";

    @Autowired
    public ListingUploadDomainPerissions(
            @Qualifier("createListingUploadActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("getAllListingUploadsActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("deleteListingUploadActionPermissions") DeleteActionPermissions deleteActionPermissions) {
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
    }
}
