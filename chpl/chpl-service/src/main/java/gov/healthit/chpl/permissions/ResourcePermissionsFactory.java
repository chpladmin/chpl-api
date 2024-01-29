package gov.healthit.chpl.permissions;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ResourcePermissionsFactory {
    private ChplResourcePermissions chplResourcePermissions;
    private SsoResourcePermissions ssoResourcePermissions;

    private FF4j ff4j;


    @Autowired
    public ResourcePermissionsFactory(CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO, CognitoUserService cognitoUserService,
            UserCertificationBodyMapDAO userCertificationBodyMapDAO, UserDeveloperMapDAO userDeveloperMapDAO,
            CertificationBodyDAO acbDAO, ErrorMessageUtil errorMessageUtil, UserDAO userDAO, FF4j ff4j) {
        this.ff4j = ff4j;

        this.chplResourcePermissions = new ChplResourcePermissions(userCertificationBodyMapDAO, userDeveloperMapDAO, acbDAO, errorMessageUtil, userDAO, developerDAO);
        this.ssoResourcePermissions = new SsoResourcePermissions(certificationBodyDAO, developerDAO, cognitoUserService);
    }

    public ResourcePermissions get() {
        if (ff4j.check(FeatureList.SSO)) {
            return ssoResourcePermissions;
        } else {
            return chplResourcePermissions;
        }
    }
}
