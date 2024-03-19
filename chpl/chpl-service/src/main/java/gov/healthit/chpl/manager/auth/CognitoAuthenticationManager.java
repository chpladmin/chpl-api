package gov.healthit.chpl.manager.auth;

import java.util.UUID;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

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
        return cognitoUserService.authenticate(credentials);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        return cognitoUserService.getUserInfo(cognitoId);
    }
}
