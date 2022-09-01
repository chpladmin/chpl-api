package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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

    @Autowired
    public CertifiedProductDomainPermissions(
            @Qualifier("certifiedProductUploadActionPermissions") UploadActionPermissions uploadActionPermissions,
            @Qualifier("certifiedProductUploadPiuActionPermissions") UploadPiuActionPermissions uploadPiuActionPermissions,
            @Qualifier("certifiedProductCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("certifiedProductUpdateActionPermissions") UpdateActionPermissions updateActionPermissions) {
        getActionPermissions().put(UPLOAD, uploadActionPermissions);
        getActionPermissions().put(UPLOAD_PIU, uploadPiuActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }
}
