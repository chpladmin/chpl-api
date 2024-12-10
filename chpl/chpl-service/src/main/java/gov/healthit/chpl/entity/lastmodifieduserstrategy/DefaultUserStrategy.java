package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.ChplSystemUsers;
import gov.healthit.chpl.entity.EntityAudit;

public class DefaultUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {
        if (getFF4j().check(FeatureList.SSO)) {
            entityAudit.setLastModifiedSsoUser(getCognitoSystemUserService().getSystemUserUuId());
            entityAudit.setLastModifiedUser(null);
        } else {
            entityAudit.setLastModifiedUser(ChplSystemUsers.DEFAULT_USER_ID);
            entityAudit.setLastModifiedSsoUser(null);
        }
    }
}
