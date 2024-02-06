package gov.healthit.chpl.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ResourcePermissionsFactory {
    private ChplResourcePermissions chplResourcePermissions;
    private CognitoResourcePermissions cognitoResourcePermissions;


    @Autowired
    public ResourcePermissionsFactory(CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO, CognitoUserService cognitoUserService,
            UserCertificationBodyMapDAO userCertificationBodyMapDAO, UserDeveloperMapDAO userDeveloperMapDAO,
            CertificationBodyDAO acbDAO, ErrorMessageUtil errorMessageUtil, UserDAO userDAO) {

        this.chplResourcePermissions = new ChplResourcePermissions(userCertificationBodyMapDAO, userDeveloperMapDAO, acbDAO, errorMessageUtil, userDAO, developerDAO);
        this.cognitoResourcePermissions = new CognitoResourcePermissions(certificationBodyDAO, developerDAO, cognitoUserService);
    }

    public ResourcePermissions get() {
        JWTAuthenticatedUser user = AuthUtil.getCurrentUser();
        if (user == null || user.getAuthenticationSystem().equals(AuthenticationSystem.CHPL)) {
            return chplResourcePermissions;
        } else {
            return cognitoResourcePermissions;
        }
    }
}
