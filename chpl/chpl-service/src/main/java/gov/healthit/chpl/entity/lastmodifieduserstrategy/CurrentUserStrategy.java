package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.AuthUtil;

public class CurrentUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {
        if (getFF4j().check(FeatureList.SSO)) {
            entityAudit.setLastModifiedSsoUser(AuthUtil.getAuditCognitoUserId());
            entityAudit.setLastModifiedUser(null);
        } else {
            entityAudit.setLastModifiedUser(AuthUtil.getAuditId());
            entityAudit.setLastModifiedSsoUser(null);
        }
    }

}
