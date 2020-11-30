package gov.healthit.chpl.permissions.domains;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.realworldtesting.UploadActionPermissions;

@Component
public class RealWorldTestingDomainPermissions  extends DomainPermissions {
    public static final String UPLOAD = "UPLOAD";

    @Autowired
    public RealWorldTestingDomainPermissions(
            @Qualifier("realWorldTestingUploadActionPermissions") UploadActionPermissions uploadActionPermissions) {

        getActionPermissions().put(UPLOAD, uploadActionPermissions);
    }
}
