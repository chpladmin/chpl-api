package gov.healthit.chpl.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ResourcePermissionsFactory {
    private CognitoResourcePermissions cognitoResourcePermissions;


    @Autowired
    public ResourcePermissionsFactory(CertificationBodyDAO certificationBodyDAO,
            DeveloperDAO developerDAO,
            CognitoApiWrapper cognitoApiWrapper,
           ErrorMessageUtil errorMessageUtil) {
        this.cognitoResourcePermissions = new CognitoResourcePermissions(certificationBodyDAO, developerDAO, cognitoApiWrapper, errorMessageUtil);
    }

    public ResourcePermissions get() {
        return cognitoResourcePermissions;
    }
}
