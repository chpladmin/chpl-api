package gov.healthit.chpl.manager.auth;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoAuthenticationManager {

    private CognitoUserService cognitoUserService;
    private FF4j ff4j;

    @Autowired
    public CognitoAuthenticationManager(CognitoUserService cognitoUserService, FF4j ff4j) {
        this.cognitoUserService = cognitoUserService;
        this.ff4j = ff4j;
    }

    public String authenticate(LoginCredentials credentials) {
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This feature has not been implemented");
        }

        return cognitoUserService.authenticate(credentials);
    }

    //TODO OCD-4203 - this will need to be uncommented when we implement more of the Cognito security server side
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID ssoUserId) throws UserRetrievalException {
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This feature has not been implemented");
        }

        return cognitoUserService.getUserInfo(ssoUserId);
    }
}
