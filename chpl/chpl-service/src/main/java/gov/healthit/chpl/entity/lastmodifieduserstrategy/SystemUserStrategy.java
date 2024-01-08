package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.CognitoSystemUsers;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.entity.EntityAudit;

public class SystemUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {
        if (getFF4j().check(FeatureList.SSO)) {
            entityAudit.setLastModifiedSsoUser(CognitoSystemUsers.SYSTEM_USER_ID);
            entityAudit.setLastModifiedUser(null);
        } else {
            entityAudit.setLastModifiedUser(SystemUsers.SYSTEM_USER_ID);
            entityAudit.setLastModifiedSsoUser(null);
        }
    }

}
