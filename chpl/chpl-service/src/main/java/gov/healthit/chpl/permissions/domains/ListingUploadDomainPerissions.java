package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.listingUpload.ConfirmActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.GetListingUploadAsListingPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.GetUploadedCsvActionPermissions;
import gov.healthit.chpl.permissions.domains.listingUpload.ValidateByIdsActionPermissions;

@Component
public class ListingUploadDomainPerissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_UPLOAD_AS_LISTING = "GET_UPLOAD_AS_LISTING";
    public static final String VALIDATE_BY_IDS = "VALIDATE_BY_IDS";
    public static final String CONFIRM = "CONFIRM";
    public static final String DELETE = "DELETE";
    public static final String GET_UPLOADED_CSV = "GET_UPLOADED_CSV";

    @Autowired
    public ListingUploadDomainPerissions(
            @Qualifier("createListingUploadActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("getAllListingUploadsActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("getListingUploadByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("getListingUploadAsListingActionPermissions") GetListingUploadAsListingPermissions getListingUploadAsListingPermissions,
            @Qualifier("validateListingUploadByIdsActionPermissions") ValidateByIdsActionPermissions validateByIdsActionPermissions,
            @Qualifier("confirmListingUploadActionPermissions") ConfirmActionPermissions confirmActionPermissions,
            @Qualifier("deleteListingUploadActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("getUploadedCsvActionPermissions") GetUploadedCsvActionPermissions getUploadedCsvActionPermissions) {
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_UPLOAD_AS_LISTING, getListingUploadAsListingPermissions);
        getActionPermissions().put(VALIDATE_BY_IDS, validateByIdsActionPermissions);
        getActionPermissions().put(CONFIRM, confirmActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(GET_UPLOADED_CSV, getUploadedCsvActionPermissions);
    }
}
