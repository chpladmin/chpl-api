package gov.healthit.chpl.permissions;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ResourcePermissionsFactory {
    private ChplResourcePermissions chplResourcePermissions;
    private CognitoResourcePermissions cognitoResourcePermissions;
    private FF4j ff4j;


    @Autowired
    public ResourcePermissionsFactory(CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO, CognitoApiWrapper cognitoApiWrapper,
            UserCertificationBodyMapDAO userCertificationBodyMapDAO, UserDeveloperMapDAO userDeveloperMapDAO,
            CertificationBodyDAO acbDAO, ErrorMessageUtil errorMessageUtil, UserDAO userDAO, FF4j ff4j) {

        this.chplResourcePermissions = new ChplResourcePermissions(userCertificationBodyMapDAO, userDeveloperMapDAO, acbDAO, errorMessageUtil, userDAO, developerDAO);
        this.cognitoResourcePermissions = new CognitoResourcePermissions(certificationBodyDAO, developerDAO, cognitoApiWrapper, errorMessageUtil);
        this.ff4j = ff4j;
    }

    public ResourcePermissions get() {
        if (ff4j.check(FeatureList.SSO)) {
            return cognitoResourcePermissions;
        } else {
            return chplResourcePermissions;
        }
    }

    public ResourcePermissions get(AuthenticationSystem authSystem) {
        if (authSystem.equals(AuthenticationSystem.CHPL)) {
            return chplResourcePermissions;
        } else if (authSystem.equals(AuthenticationSystem.COGNITO)) {
            return cognitoResourcePermissions;
        } else {
            return null;
        }
    }
}
