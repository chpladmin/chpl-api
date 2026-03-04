package gov.healthit.chpl.permissions.domains;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.realworldtesting.UploadActionPermissions;
import gov.healthit.chpl.permissions.domains.realworldtesting.ValidateUrlActionPermissions;

@Component
public class RealWorldTestingDomainPermissions  extends DomainPermissions {
    public static final String UPLOAD = "UPLOAD";
    public static final String VALIDATE_URL = "VALIDATE_URL";

    @Autowired
    public RealWorldTestingDomainPermissions(
            @Qualifier("realWorldTestingUploadActionPermissions") UploadActionPermissions uploadActionPermissions,
            @Qualifier("realWorldTestingValidateUrlActionPermissions") ValidateUrlActionPermissions validateUrlActionPermissions) {

        getActionPermissions().put(UPLOAD, uploadActionPermissions);
        getActionPermissions().put(VALIDATE_URL, validateUrlActionPermissions);
    }
}
