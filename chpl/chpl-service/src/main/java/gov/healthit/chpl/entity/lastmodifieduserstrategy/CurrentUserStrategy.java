package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.AuthUtil;

public class CurrentUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populationLastModifiedUser(EntityAudit entityAudit) {
        if (getFF4j().check(FeatureList.SSO)) {
            entityAudit.setLastModifiedSsoUser(AuthUtil.getAuditSsoUser());
            entityAudit.setLastModifiedUser(null);
        } else {
            entityAudit.setLastModifiedUser(AuthUtil.getAuditId());
            entityAudit.setLastModifiedSsoUser(null);
        }
    }

}
