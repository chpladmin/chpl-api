package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.user.cognito.CognitoUserManager;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("cognitoUpdatePasswordActionPermissions")
public class CognitoUpdatePasswordActionPermissions extends ActionPermissions {

    private CognitoUserManager cognitoUserManager;

    @Autowired
    public CognitoUpdatePasswordActionPermissions(CognitoUserManager cognitoUserManager) {
        this.cognitoUserManager = cognitoUserManager;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof String)) {
            return false;
        } else {
            String email = (String) obj;
            try {
                User user = cognitoUserManager.getUserInfo(AuthUtil.getCurrentUser().getCognitoId());
                return user.getEmail().equalsIgnoreCase(email);
            } catch (UserRetrievalException e) {
                LOGGER.error("Could not retrieve user: {}", email, e);
                return false;
            }
        }
    }

}
