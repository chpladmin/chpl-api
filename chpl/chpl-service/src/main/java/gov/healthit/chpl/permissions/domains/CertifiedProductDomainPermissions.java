package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certifiedproduct.ChangeAcbOwnerActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.ConvertToCsvActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UploadActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UploadPiuActionPermissions;

@Component
public class CertifiedProductDomainPermissions extends DomainPermissions {
    public static final String UPLOAD = "UPLOAD";
    public static final String UPLOAD_PIU = "UPLOAD_PIU";
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String CONVERT_TO_CSV = "CONVERT_TO_CSV";
    public static final String CHANGE_ACB_OWNER = "CHANGE_ACB_OWNER";

    @Autowired
    public CertifiedProductDomainPermissions(
            @Qualifier("certifiedProductUploadActionPermissions") UploadActionPermissions uploadActionPermissions,
            @Qualifier("certifiedProductUploadPiuActionPermissions") UploadPiuActionPermissions uploadPiuActionPermissions,
            @Qualifier("certifiedProductCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("certifiedProductUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("certifiedProductConvertToCsvActionPermissions") ConvertToCsvActionPermissions convertToCsvActionPermissions,
            @Qualifier("certifiedProductChangeAcbOwnerActionPermissions") ChangeAcbOwnerActionPermissions changeAcbOwnerActionPermissions) {
        getActionPermissions().put(UPLOAD, uploadActionPermissions);
        getActionPermissions().put(UPLOAD_PIU, uploadPiuActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CONVERT_TO_CSV, convertToCsvActionPermissions);
        getActionPermissions().put(CHANGE_ACB_OWNER, changeAcbOwnerActionPermissions);
    }
}
