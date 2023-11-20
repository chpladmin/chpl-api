package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.CognitoSystemUsers;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.AuthUtil;

public class CurrentUserThenSystemUserStrategy extends LastModifiedUserStrategy {
    @Override
    public void populationLastModifiedUser(EntityAudit entityAudit) {


        if (getFF4j().check(FeatureList.SSO)) {
            if (AuthUtil.getCurrentUser() != null) {
                entityAudit.setLastModifiedSsoUser(AuthUtil.getAuditSsoUser());
                entityAudit.setLastModifiedUser(null);
            } else {
                entityAudit.setLastModifiedSsoUser(CognitoSystemUsers.SYSTEM_USER_ID);
                entityAudit.setLastModifiedUser(null);
            }
        } else {
            if (AuthUtil.getCurrentUser() != null) {
                entityAudit.setLastModifiedSsoUser(null);
                entityAudit.setLastModifiedUser(AuthUtil.getAuditId());
            } else {
                entityAudit.setLastModifiedSsoUser(null);
                entityAudit.setLastModifiedUser(SystemUsers.SYSTEM_USER_ID);
            }
        }
    }
}
