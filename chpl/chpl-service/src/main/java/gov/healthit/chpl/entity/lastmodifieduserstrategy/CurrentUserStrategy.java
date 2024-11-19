package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.AuthUtil;

public class CurrentUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {
        if (AuthUtil.getCurrentUser() != null) {
            if (AuthUtil.getCurrentUser().getAuthenticationSystem().equals(AuthenticationSystem.COGNITO)) {
                entityAudit.setLastModifiedSsoUser(AuthUtil.getCurrentUser().getCognitoId());
                entityAudit.setLastModifiedUser(null);
            } else {
                entityAudit.setLastModifiedUser(AuthUtil.getCurrentUser().getId());
                entityAudit.setLastModifiedSsoUser(null);
            }
        } else {
            entityAudit.setLastModifiedSsoUser(getCognitoSystemUserService().getAnonymousUserUuId());
            entityAudit.setLastModifiedUser(null);
        }
    }

}
