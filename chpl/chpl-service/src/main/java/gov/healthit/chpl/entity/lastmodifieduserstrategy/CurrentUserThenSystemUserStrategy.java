package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.auth.user.CognitoSystemUsers;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.AuthUtil;

public class CurrentUserThenSystemUserStrategy extends LastModifiedUserStrategy {
    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {


        if (AuthUtil.getCurrentUser() != null) {
            if (AuthUtil.getCurrentUser().getAuthenticationSystem().equals(AuthenticationSystem.COGNITO)) {
                entityAudit.setLastModifiedSsoUser(AuthUtil.getCurrentUser().getCognitoId());
                entityAudit.setLastModifiedUser(null);
            } else {
                entityAudit.setLastModifiedSsoUser(null);
                entityAudit.setLastModifiedUser(AuthUtil.getCurrentUser().getId());
            }

        } else {
            entityAudit.setLastModifiedSsoUser(CognitoSystemUsers.SYSTEM_USER_ID);
            entityAudit.setLastModifiedUser(null);
        }
    }
}
