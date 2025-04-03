package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.entity.EntityAudit;

public class DefaultUserStrategy extends LastModifiedUserStrategy {

    @Override
    public void populateLastModifiedUser(EntityAudit entityAudit) {
        entityAudit.setLastModifiedSsoUser(getCognitoSystemUserService().getSystemUserUuId());
        entityAudit.setLastModifiedUser(null);
    }
}
