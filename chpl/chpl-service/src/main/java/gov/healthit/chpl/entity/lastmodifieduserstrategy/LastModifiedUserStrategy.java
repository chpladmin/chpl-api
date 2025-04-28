package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import gov.healthit.chpl.SpringContext;
import gov.healthit.chpl.auth.user.CognitoSystemUserService;
import gov.healthit.chpl.entity.EntityAudit;

public abstract class LastModifiedUserStrategy {
    private CognitoSystemUserService cognitoSystemUserService = SpringContext.getBean(CognitoSystemUserService.class);

    public abstract void populateLastModifiedUser(EntityAudit entityAudit);

    public CognitoSystemUserService getCognitoSystemUserService() {
        return cognitoSystemUserService;
    }
}
